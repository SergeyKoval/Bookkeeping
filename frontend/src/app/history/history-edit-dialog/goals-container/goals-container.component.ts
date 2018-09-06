import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';

import { IMyDate } from 'mydatepicker';

import { BudgetService } from '../../../common/service/budget.service';
import { GoalFilterType } from '../../../common/model/history/GoalFilterType';
import { DateUtils } from '../../../common/utils/date-utils';

@Component({
  selector: 'bk-goals-container',
  templateUrl: './goals-container.component.html',
  styleUrls: ['./goals-container.component.css']
})
export class GoalsContainerComponent implements OnInit {
  public FILTER_TYPES: GoalFilterType[] = [GoalFilterType.NOT_DONE, GoalFilterType.DONE, GoalFilterType.ALL];

  @Input()
  public historyItem: HistoryType;
  @Input()
  public editMode: boolean;
  @Input()
  public alternativeCurrencyLoading: boolean;
  @Input()
  public set selectedCategory(value: string) {
    if (this._selectedCategory !== value && this._budget) {
      this._selectedCategory = value;
      this.chooseBudgetCategory(this._budget);
    } else {
      this._selectedCategory = value;
    }
  }
  @Input()
  public set selectedDate(value: IMyDate) {
    const dateChanged: boolean = this._selectedDate && (this._selectedDate.year !== value.year || this._selectedDate.month !== value.month);
    this._selectedDate = value;

    if (dateChanged) {
      this.updateMonthProgress();
      this.loadBudget();
    }
  }

  @Output()
  public statusChange: EventEmitter<boolean> = new EventEmitter();

  public goalFilterType: GoalFilterType;
  public budgetLoading: boolean;
  public monthProgress: MonthProgress;
  public budgetCategory: BudgetCategory;
  public selectedGoal: BudgetGoal;

  private _budget: Budget;
  private _selectedCategory: string;
  private _selectedDate: IMyDate;
  private _selectedGoalChangeStatus: boolean = false;
  private _originalBudget: Budget;
  private _originalCategory: BudgetCategory;
  private _originalGoalStatus: boolean;

  public constructor(private _budgetService: BudgetService) { }

  public ngOnInit(): void {
    this.goalFilterType = this.editMode ? GoalFilterType.ALL : GoalFilterType.NOT_DONE;
    this.updateMonthProgress();
    this.loadBudget();
  }

  public getGoalCount(filterType: GoalFilterType): number {
    return this.budgetCategory && this.budgetCategory.goals ? BudgetService.filterGoals(this.budgetCategory.goals, filterType).length : 0;
  }

  public getPercentBeforeSelection(goalItem: BudgetGoal): number {
    if (goalItem.done === true) {
      if (!this.isSelectedGoal(goalItem) || goalItem.balance.value >= goalItem.balance.completeValue) {
        return 100;
      } else {
        const valuePercent: number = Math.round(this.convertValueToCurrency(goalItem.balance.currency) / goalItem.balance.completeValue * 100);
        return 100 - (valuePercent < 100 ? valuePercent : 100);
      }
    }

    const percent: number = Math.round(goalItem.balance.value / goalItem.balance.completeValue * 100);
    return percent < 100 ? percent : 100;
  }

  public getSelectedPercent(goalItem: BudgetGoal): number {
    const previousPercent: number = this.getPercentBeforeSelection(goalItem);
    const selectedGoalAffectedValue: number = this.convertValueToCurrency(goalItem.balance.currency);
    const selectedPercent: number = Math.round(selectedGoalAffectedValue / this.selectedGoal.balance.completeValue * 100);
    return (selectedPercent + previousPercent) < 100 ? selectedPercent : (100 - previousPercent);
  }

  public getGoalValue(goalItem: BudgetGoal): number {
    return goalItem !== this.selectedGoal ? goalItem.balance.value : (goalItem.balance.value + this.convertValueToCurrency(goalItem.balance.currency));
  }

  public getGoalPercent(goalItem: BudgetGoal): number {
    return this.getGoalValue(goalItem) / goalItem.balance.completeValue * 100;
  }

  public chooseGoal(goalItem: BudgetGoal): void {
    if (goalItem.title !== this.historyItem.goal) {
      this.revertSelectedGoalOriginalStatus();
      this.selectedGoal = goalItem;
      this.historyItem.goal = goalItem.title;
    }
  }

  public uncheckGoal(): void {
    this.revertSelectedGoalOriginalStatus();
    this.selectedGoal = null;
    this.historyItem.goal = null;
  }

  public changeGoalStatus(goalItem: BudgetGoal): void {
    if (goalItem.title === this.historyItem.goal) {
      this.selectedGoal.done = !this.selectedGoal.done;
      this._selectedGoalChangeStatus = !this._selectedGoalChangeStatus;
      this.statusChange.next(this._selectedGoalChangeStatus);
    }
  }

  public getGoalDonePopoverTitle(goal: BudgetGoal): string {
    return goal.done
      ? this.isSelectedGoal(goal) ? 'Пометить невыполненным' : 'Выполнено'
      : this.isSelectedGoal(goal) ? 'Пометить выполненным' : 'Не выполнено';
  }

  public isSelectedGoal(goal: BudgetGoal): boolean {
    return goal.title === this.historyItem.goal;
  }

  public get selectedCategory(): string {
    return this._selectedCategory;
  }

  public get selectedDate(): IMyDate {
    return this._selectedDate;
  }

  private loadBudget(): void {
    this.budgetLoading = true;
    this._budgetService.loadBudget(this._selectedDate.year, this._selectedDate.month).subscribe((budget: Budget) => {
      if (this.editMode && this.historyItem.goal && !this._budget) {
        this.populateOriginalGoal(budget);
      }
      this._budget = budget;
      this.chooseBudgetCategory(budget);
      this.budgetLoading = false;
    });
  }

  private updateMonthProgress(): void {
    const now: Date = new Date();
    const currentMonth: boolean = now.getFullYear() === this._selectedDate.year && now.getMonth() === this._selectedDate.month - 1;
    const monthPercent: number = currentMonth ? Math.round(now.getDate() / DateUtils.daysInMonth(this._selectedDate.year, this._selectedDate.month) * 100) : 0;
    this.monthProgress = {'currentMonth': currentMonth, 'monthPercent': monthPercent};
  }

  private populateOriginalGoal(budget: Budget): void {
    this._originalBudget = budget;
    this._originalCategory = budget[this.historyItem.type].categories.filter(category => category.title === this.selectedCategory)[0];
    this._originalGoalStatus = this._originalCategory.goals.filter(goal => goal.title === this.historyItem.goal)[0].done;
  }

  private convertValueToCurrency(resultCurrency: string): number {
    const balance: HistoryBalanceType = this.historyItem.balance;
    if (balance.currency === resultCurrency || !balance.value) {
      return balance.value || 0;
    }

    return balance.alternativeCurrency[resultCurrency];
  }

  private chooseBudgetCategory(budget: Budget): void {
    this.budgetCategory = budget[this.historyItem.type].categories.filter(category => category.title === this._selectedCategory)[0];
    if (this.budgetCategory && this.budgetCategory.goals) {
      this.budgetCategory.goals = BudgetService.sortGoals(this.budgetCategory.goals, this.selectedGoal);
    }
  }

  private revertSelectedGoalOriginalStatus(): void {
    if (this._selectedGoalChangeStatus === true && this.selectedGoal) {
      this.selectedGoal.done = !this.selectedGoal.done;
      this._selectedGoalChangeStatus = false;
    }
  }
}
