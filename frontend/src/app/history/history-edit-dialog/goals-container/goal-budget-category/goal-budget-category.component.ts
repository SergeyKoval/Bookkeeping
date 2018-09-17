import { Component, Input } from '@angular/core';

import { IMyDate } from 'mydatepicker';

import { BudgetService } from '../../../../common/service/budget.service';
import { ProfileService } from '../../../../common/service/profile.service';
import { CurrencyValuePipe } from '../../../../common/pipes/currency-value.pipe';

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

  public constructor(
    private _budgetService: BudgetService,
    private _profileService: ProfileService,
    private _currencyValuePipe: CurrencyValuePipe
  ) { }

  public getNumberOfCurrencies(): number {
    if (!this.budgetCategory) {
      return 1;
    }

    const numberOfCurrencies: number = Object.keys(this.budgetCategory.balance).length;
    return numberOfCurrencies > 0 ? numberOfCurrencies : 1;
  }

  public getPercentBeforeAction(): number {
    return this._budgetService.calculatePercentDone(this.budgetCategory.balance);
  }

  public getActionPercent(): number {
    const previousPercent: number = this.getPercentBeforeAction();
    const fullPercent: number = this.getPercentWithHistoryItem();
    return fullPercent < 100 ? fullPercent - previousPercent : (100 - previousPercent);
  }

  public getCurrentBudgetStyle(): string {
    const fullPercent: number = this.getPercentWithHistoryItem();
    if (!this.monthProgress.currentMonth) {
      return (this.historyItem.type === 'expense' && fullPercent <= 100) || (this.historyItem.type === 'income' && fullPercent >= 100) ? 'progress-bar-success' : 'progress-bar-warning';
    }

    const dayPercent: number = this.monthProgress.monthPercent;
    return (this.historyItem.type === 'expense' && dayPercent >= fullPercent) || (this.historyItem.type === 'income' && dayPercent <= fullPercent) ? 'progress-bar-success' : 'progress-bar-warning';
  }

  public getBudgetValue(currency: string): string {
    const value: number = currency === this.historyItem.balance.currency
      ? this.budgetCategory.balance[currency].value + this.getActionValue()
      : this.budgetCategory.balance[currency].value;
    return this._currencyValuePipe.transform(value, 2, true);
  }

  private getActionValue(): number {
    return !this.historyItem.balance.value ? 0 : this.historyItem.balance.value;
  }

  private getPercentWithHistoryItem(): number {
    const defaultCurrency: string = this._profileService.defaultCurrency.name;
    const valueInDefaultCurrency: number = defaultCurrency === this.historyItem.balance.currency ? this.historyItem.balance.value : this.historyItem.balance.alternativeCurrency[defaultCurrency];
    return this._budgetService.calculatePercentDone(this.budgetCategory.balance, valueInDefaultCurrency);
  }
}
