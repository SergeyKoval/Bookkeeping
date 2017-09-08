import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';

import { IMyDate } from 'mydatepicker';

import { BudgetService } from '../../../../common/service/budget.service';
import { AuthenticationService } from '../../../../common/service/authentication.service';

import 'rxjs/add/operator/first';

@Component({
  selector: 'bk-goals-container',
  templateUrl: './goals-container.component.html',
  styleUrls: ['./goals-container.component.css']
})
export class GoalsContainerComponent implements OnInit {
  @Input()
  public historyItem: HistoryType;
  @Input()
  public goal: HistoryGoal;
  @Input()
  public selectedCurrency: string;
  @Input()
  public editMode: boolean;
  @Output()
  public goalChange: EventEmitter<HistoryGoal> = new EventEmitter();
  @Output()
  public statusChange: EventEmitter<boolean> = new EventEmitter();

  public selectedGoal: BudgetGoal;
  public isCategoryGoalsSelected: boolean;
  public categoryBudgetItem: BudgetItem;
  public otherGoals: BudgetGoal[];
  public categoryLoading: boolean = true;

  private _selectedGoalChangeStatus: boolean = false;
  private _selectedCategory: string;
  private _selectedDate: IMyDate;
  private _selectedValue: number;
  private _alternativeCurrencies: {[key: string]: number};
  private _selectedGoalAffectedValue: number = 0;
  private _originallySelectedGoal: BudgetGoal;

  public constructor(
    private _budgetService: BudgetService,
    private _authenticationService: AuthenticationService
  ) { }

  @Input()
  public set selectedValue(value: number) {
    // if (this.selectedGoal) {
    //   this._selectedGoalAffectedValue = this.convertValueToCurrency(value, this.selectedCurrency, this.selectedGoal.balance.currency, this._alternativeCurrencies);
    // }
    this._selectedValue = value;
  }

  @Input()
  public set alternativeCurrencies(value: {[p: string]: number}) {
    // if (this.selectedGoal) {
    //   this._selectedGoalAffectedValue = this.convertValueToCurrency(this._selectedValue, this.selectedCurrency, this.selectedGoal.balance.currency, value);
    // }
    this._alternativeCurrencies = value;
  }

  @Input()
  public set selectedDate(value: IMyDate) {
    // if (this._selectedDate && (this._selectedDate.year !== value.year || this._selectedDate.month !== value.month)) {
    //   this.categoryLoading = true;
    //   this.categoryBudgetItem = null;
    //   this.otherGoals = null;
    //   this.loadCategoryGoals(value);
    //   this.loadOtherGoals(value);
    // }

    this._selectedDate = value;
  }

  @Input()
  public set selectedCategory(value: string) {
    const needToReload: boolean = this._selectedCategory && this.selectedCategory !== value;
    this._selectedCategory = value;
    if (needToReload) {
      this.categoryLoading = true;
      this.categoryBudgetItem = null;
      this.loadCategoryGoals(this._selectedDate);
    }
  }

  public ngOnInit(): void {
    this.isCategoryGoalsSelected = !this.goal || this.goal.category === this._selectedCategory;
    this.loadCategoryGoals(this._selectedDate);
    this.loadOtherGoals(this._selectedDate);
  }

  // public displayLoadingIndicator(): boolean {
  //   return (this.isCategoryGoalsSelected && this.categoryLoading) || (!this.isCategoryGoalsSelected && !this.otherGoals);
  // }
  //
  // public getSelectedGoals(): BudgetGoal[] {
  //   return this.isCategoryGoalsSelected ? (this.categoryBudgetItem ? this.categoryBudgetItem.goals : []) : this.otherGoals;
  // }
  //
  // public chooseGoal(goalItem: BudgetGoal): void {
  //   if (goalItem !== this.selectedGoal) {
  //     if (this.selectedGoal && this._selectedGoalChangeStatus) {
  //       this.selectedGoal.done = !this.selectedGoal.done;
  //     }
  //
  //     this._selectedGoalChangeStatus = false;
  //     this.statusChange.next(this._selectedGoalChangeStatus);
  //
  //     // TODO: question about previous selected goal
  //     this.selectedGoal = goalItem;
  //     this.goalChange.next({category: this.isCategoryGoalsSelected ? this.selectedCategory : null, name: this.selectedGoal.name});
  //     this._selectedGoalAffectedValue = this.convertValueToCurrency(this._selectedValue, this.selectedCurrency, this.selectedGoal.balance.currency, this._alternativeCurrencies);
  //   }
  // }
  //
  // public uncheckGoal(): void {
  //   this.goal = null;
  //   this.selectedGoal = null;
  // }
  //
  // public changeGoalStatus(goalItem: BudgetGoal): void {
  //   if (goalItem === this.selectedGoal) {
  //     this._selectedGoalChangeStatus = !this._selectedGoalChangeStatus;
  //     this.statusChange.next(this._selectedGoalChangeStatus);
  //     this.selectedGoal.done = !this.selectedGoal.done;
  //   }
  // }
  //
  // public getPercentBeforeSelection(goalItem: BudgetGoal): number {
  //   const value: number = (goalItem === this.selectedGoal) && (this.selectedGoal === this._originallySelectedGoal)
  //     ? (goalItem.balance.value - this._selectedGoalAffectedValue) : goalItem.balance.value;
  //   const percent: number = Math.round(value / goalItem.balance.completeValue * 100);
  //   return percent < 100 ? percent : 100;
  // }
  //
  // public getGoalValue(goalItem: BudgetGoal): number {
  //   return goalItem !== this.selectedGoal || (goalItem === this.selectedGoal && this.selectedGoal === this._originallySelectedGoal)
  //     ? goalItem.balance.value : (goalItem.balance.value + this._selectedGoalAffectedValue);
  // }
  //
  // public getGoalPercent(goalItem: BudgetGoal): number {
  //   return this.getGoalValue(goalItem) / goalItem.balance.completeValue * 100;
  // }
  //
  // public getSelectedPercent(goalItem: BudgetGoal): number {
  //   const previousPercent: number = this.getPercentBeforeSelection(goalItem);
  //   const selectedPercent: number = Math.round(this._selectedGoalAffectedValue / this.selectedGoal.balance.completeValue * 100);
  //   return (selectedPercent + previousPercent) < 100 ? selectedPercent : (100 - previousPercent);
  // }
  //
  // public get selectedCategory(): string {
  //   return this._selectedCategory;
  // }
  //
  public get selectedDate(): IMyDate {
    return this._selectedDate;
  }

  private loadCategoryGoals(selectedDate: IMyDate): void {
    this._budgetService.loadBudgetItem(this._authenticationService.authenticatedProfile.id, selectedDate.year, selectedDate.month, this._selectedCategory, this.historyItem.type)
      .subscribe((budgetItems: BudgetItem[]) => {
        this.categoryBudgetItem = budgetItems.length === 1 ? budgetItems[0] : null;
        if (this.categoryBudgetItem && this.goal && this.goal.category === this._selectedCategory) {
          this.selectedGoal = this.categoryBudgetItem.goals.filter((budgetGoal: BudgetGoal) => budgetGoal.name === this.goal.name).shift();
        }
        this.categoryLoading = false;
      });
  }

  private loadOtherGoals(selectedDate: IMyDate): void {
    this._budgetService.loadBudgetItem(this._authenticationService.authenticatedProfile.id, selectedDate.year, selectedDate.month, '', this.historyItem.type)
      .subscribe((budgetItems: BudgetItem[]) => {
        this.otherGoals = budgetItems.length === 1 ? budgetItems[0].goals : [];
        if (this.goal && this.goal.category === null) {
          this.selectedGoal = this.otherGoals.filter((budgetGoal: BudgetGoal) => budgetGoal.name === this.goal.name).shift();
        }
      });
  }
  //
  // private convertValueToCurrency(value: number, valueCurrency: string, goalCurrency: string, alternativeCurrencies: {[key: string]: number}): number {
  //   if (valueCurrency === goalCurrency) {
  //     return value || 0;
  //   }
  //
  //   return Math.round(((value || 0) * alternativeCurrencies[goalCurrency]) * 100) / 100 ;
  // }
}
