import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';

import { IMyDate } from 'mydatepicker';

import { BudgetService } from '../../../../common/service/budget.service';
import { ProfileService } from '../../../../common/service/profile.service';
import { GoalFilterType } from '../../../../common/model/history/GoalFilterType';
import { HistoryService } from '../../../../common/service/history.service';

@Component({
  selector: 'bk-goals-container',
  templateUrl: './goals-container.component.html',
  styleUrls: ['./goals-container.component.css']
})
export class GoalsContainerComponent implements OnInit {
  @Input()
  public historyItem: HistoryType;
  @Input()
  public editMode: boolean;
  @Input()
  public alternativeCurrencyLoading: boolean;
  @Output()
  public statusChange: EventEmitter<boolean> = new EventEmitter();

  public selectedGoal: BudgetGoal;
  public categoryLoading: boolean = true;
  public goalFilterType: GoalFilterType;

  private _budgetCategory: BudgetCategory;
  private _selectedCategory: string;
  private _selectedDate: IMyDate;
  private _originallySelectedBudget: Budget;
  private _selectedGoalChangeStatus: boolean = false;

  public constructor(
    private _budgetService: BudgetService,
    private _historyService: HistoryService,
    private _authenticationService: ProfileService
  ) { }

  @Input()
  public set selectedDate(value: IMyDate) {
    if (this._selectedDate && (this._selectedDate.year !== value.year || this._selectedDate.month !== value.month)) {
      this.loadBudget(value);
    }

    this._selectedDate = value;
  }

  @Input()
  public set selectedCategory(value: string) {
    if (this._selectedCategory !== value) {
      this._selectedCategory = value;
      this.loadBudget(this._selectedDate);
    }
  }

  public ngOnInit(): void {
    this.goalFilterType = this.editMode ? GoalFilterType.ALL : GoalFilterType.NOT_DONE;
  }

  public getPercentBeforeSelection(goalItem: BudgetGoal): number {
    const percent: number = Math.round(goalItem.balance.value / goalItem.balance.completeValue * 100);
    return percent < 100 ? percent : 100;
  }

  public getSelectedPercent(goalItem: BudgetGoal): number {
    const previousPercent: number = this.getPercentBeforeSelection(goalItem);
    const selectedGoalAffectedValue: number = this.convertValueToCurrency(goalItem.balance.currency);
    const selectedPercent: number = Math.round(selectedGoalAffectedValue / this.selectedGoal.balance.completeValue * 100);
    return (selectedPercent + previousPercent) < 100 ? selectedPercent : (100 - previousPercent);
  }

  public getGoalPercent(goalItem: BudgetGoal): number {
    return this.getGoalValue(goalItem) / goalItem.balance.completeValue * 100;
  }

  public getGoalValue(goalItem: BudgetGoal): number {
    return goalItem !== this.selectedGoal ? goalItem.balance.value : (goalItem.balance.value + this.convertValueToCurrency(goalItem.balance.currency));
  }

  public changeGoalStatus(goalItem: BudgetGoal): void {
    if (goalItem.name === this.historyItem.goal) {
      this.selectedGoal.done = !this.selectedGoal.done;
      this._selectedGoalChangeStatus = !this._selectedGoalChangeStatus;
      this.statusChange.next(this._selectedGoalChangeStatus);
    }
  }

  public chooseGoal(goalItem: BudgetGoal): void {
    if (goalItem.name !== this.historyItem.goal) {
      this.revertSelectedGoalOriginalStatus();

      this.selectedGoal = goalItem;
      this.historyItem.goal = goalItem.name;
    }
  }

  public uncheckGoal(): void {
    this.revertSelectedGoalOriginalStatus();
    this.selectedGoal = null;
    this.historyItem.goal = null;
  }

  public getGoalCount(filterType: GoalFilterType): number {
    return this.budgetCategory !== null ? HistoryService.filterGoals(this.budgetCategory.goals, filterType).length : 0;
  }

  public isSelectedGoal(goal: BudgetGoal): boolean {
    return goal.name === this.historyItem.goal;
  }

  public chooseGoalFilterType(type: GoalFilterType): void {
    this.goalFilterType = type;
  }

  public isSelectedGoalFilterType(type: GoalFilterType): boolean {
    return this.goalFilterType === type;
  }

  public getAllGoalFilterTypes(): GoalFilterType[] {
    return [GoalFilterType.NOT_DONE, GoalFilterType.DONE, GoalFilterType.ALL];
  }

  public get selectedDate(): IMyDate {
    return this._selectedDate;
  }

  public get budgetCategory(): BudgetCategory {
    return this._budgetCategory;
  }

  private loadBudget(selectedDate: IMyDate): void {
    this.categoryLoading = true;
    this._budgetCategory = null;
    const ownerId: number = this._authenticationService.authenticatedProfile.id;

    if (!this.editMode || (this.editMode && this._originallySelectedBudget)) {
      this.historyItem.goal = null;
    }

    if (this.editMode && this._originallySelectedBudget && selectedDate.year === this._originallySelectedBudget.year
       && selectedDate.month === this._originallySelectedBudget.month && this.historyItem.type === this._originallySelectedBudget.type) {

      this._budgetCategory = this._originallySelectedBudget.budgetCategories.filter((budgetCategory: BudgetCategory) => budgetCategory.category === this._selectedCategory)[0];
      this.categoryLoading = false;
    } else {
      this._budgetService.loadBudget(ownerId, selectedDate.year, selectedDate.month, this.historyItem.type)
        .subscribe((budget: Budget) => {
          if (budget) {
            this._budgetCategory = budget.budgetCategories.filter((budgetCategory: BudgetCategory) => budgetCategory.category === this._selectedCategory)[0];
          }

          if (this._budgetCategory && this.editMode && !this._originallySelectedBudget && selectedDate.year === budget.year
            && selectedDate.month === budget.month && this.historyItem.type === budget.type) {

            this._originallySelectedBudget = budget;
            const budgetBalance: BudgetBalance = this._historyService.chooseBudgetBalanceBasedOnCurrency(this.historyItem, this._budgetCategory);
            budgetBalance.value = budgetBalance.value - this.convertValueToCurrency(budgetBalance.currency);
            if (this.budgetCategory.goals) {
              this.selectedGoal = this.budgetCategory.goals.filter((goal: BudgetGoal) => goal.name === this.historyItem.goal)[0];
              if (this.selectedGoal) {
                this.selectedGoal.balance.value = this.selectedGoal.balance.value - this.convertValueToCurrency(this.selectedGoal.balance.currency);
              }
            } else {
              this.selectedGoal = null;
            }
          }

          this.categoryLoading = false;
        });
    }
  }

  private convertValueToCurrency(resultCurrency: string): number {
    const balance: HistoryBalanceType = this.historyItem.balance;
    if (balance.currency === resultCurrency) {
      return balance.value || 0;
    }

    return Math.round(((balance.value || 0) * balance.alternativeCurrency[resultCurrency]) * 100) / 100 ;
  }

  private revertSelectedGoalOriginalStatus(): void {
    if (this._selectedGoalChangeStatus && this.selectedGoal) {
      this.selectedGoal.done = !this.selectedGoal.done;
      this._selectedGoalChangeStatus = false;
    }
  }
}
