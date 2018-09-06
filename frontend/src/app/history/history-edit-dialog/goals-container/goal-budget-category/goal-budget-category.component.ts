import { Component, Input } from '@angular/core';

import { IMyDate } from 'mydatepicker';

import { ProfileService } from '../../../../common/service/profile.service';

@Component({
  selector: 'bk-goal-budget-category',
  templateUrl: './goal-budget-category.component.html',
  styleUrls: ['./goal-budget-category.component.css']
})
export class GoalBudgetCategoryComponent {
  @Input()
  public historyItem: HistoryType;
  @Input()
  public selectedDate: IMyDate;
  @Input()
  public budgetLoading: boolean;
  @Input()
  public alternativeCurrencyLoading: boolean;
  @Input()
  public monthProgress: MonthProgress;
  @Input()
  public budgetCategory: BudgetCategory;

  public balance: BudgetBalance;

  public constructor(private _profileService: ProfileService) { }

  public getPercentBeforeAction(): number {
    const percent: number = Math.round(this.balance.value / this.balance.completeValue * 100);
    return percent < 100 ? percent : 100;
  }

  public getActionPercent(): number {
    const previousPercent: number = this.getPercentBeforeAction();
    const selectedPercent: number = Math.round(this.getActionValue() / this.balance.completeValue * 100);
    return (selectedPercent + previousPercent) < 100 ? selectedPercent : (100 - previousPercent);
  }

  public getCurrentBudgetStyle(): string {
    const budgetValue: number = this.getBudgetValue();
    if (!this.monthProgress.currentMonth) {
      return (this.historyItem.type === 'expense' && budgetValue <= this.balance.completeValue)
        || (this.historyItem.type === 'income' && budgetValue >= this.balance.completeValue) ? 'progress-bar-success' : 'progress-bar-warning';
    }

    const dayPercent: number = this.monthProgress.monthPercent;
    const budgetPercent: number = Math.round(budgetValue / this.balance.completeValue * 100);
    return (this.historyItem.type === 'expense' && dayPercent >= budgetPercent)
      || (this.historyItem.type === 'income' && dayPercent <= budgetPercent) ? 'progress-bar-success' : 'progress-bar-warning';
  }

  public getBudgetValue(): number {
    const currentValue: number = this.getActionValue();
    return this.balance.value + currentValue;
  }

  private getActionValue(): number {
    const currency: string = this.getCurrency();
    return !this.historyItem.balance.value
      ? 0 : (currency === this.historyItem.balance.currency
        ? this.historyItem.balance.value : this.historyItem.balance.alternativeCurrency[currency]);
  }

  private getCurrency(): string {
    let currency: string = this.historyItem.balance.currency;
    let balance: BudgetBalance = this.budgetCategory.balance[currency];
    if (balance) {
      this.balance = balance;
      return currency;
    }

    currency = this._profileService.defaultCurrency.name;
    balance = this.budgetCategory.balance[currency];
    if (balance) {
      this.balance = balance;
      return currency;
    }

    currency = this._profileService.defaultCurrency.name;
    this.balance = this.budgetCategory.balance[currency];
    return currency;
  }
}
