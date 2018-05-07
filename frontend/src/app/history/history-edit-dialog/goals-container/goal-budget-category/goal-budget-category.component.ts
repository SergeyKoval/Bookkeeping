import { Component, Input } from '@angular/core';

import { IMyDate } from 'mydatepicker';

import { DateUtils } from '../../../../common/utils/date-utils';
import { CurrencyUtils } from '../../../../common/utils/currency-utils';
import { HistoryService } from '../../../../common/service/history.service';

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
  public categoryLoading: boolean;
  @Input()
  public alternativeCurrencyLoading: boolean;
  @Input()
  public budgetCategory: BudgetCategory;

  public constructor(private _historyService: HistoryService) { }

  public isCurrentMonth(): boolean {
    return (new Date(Date.now()).getUTCMonth() + 1) === this.selectedDate.month;
  }

  public getDayPercent(): number {
    return Math.round(new Date(Date.now()).getUTCDate() / DateUtils.daysInMonth(this.selectedDate.year, this.selectedDate.month) * 100);
  }

  public getCurrentBudgetStyle(budgetBalance: BudgetBalance): string {
    const budgetValue: number = this.getBudgetValue(budgetBalance);
    if (!this.isCurrentMonth()) {
      return (this.historyItem.type === 'expense' && budgetValue <= budgetBalance.completeValue)
        || (this.historyItem.type === 'income' && budgetValue >= budgetBalance.completeValue) ? 'progress-bar-success' : 'progress-bar-danger';
    }

    const dayPercent: number = this.getDayPercent();
    const budgetPercent: number = Math.round(budgetValue / budgetBalance.completeValue * 100);
    return (this.historyItem.type === 'expense' && dayPercent >= budgetPercent)
      || (this.historyItem.type === 'income' && dayPercent <= budgetPercent) ? 'progress-bar-success' : 'progress-bar-danger';
  }

  public getBudgetValue(budgetBalance: BudgetBalance): number {
    const currentValue: number = this.getActionValue(budgetBalance);
    return budgetBalance.value + currentValue;
  }

  public getBudgetBalance(): BudgetBalance {
    return this._historyService.chooseBudgetBalanceBasedOnCurrency(this.historyItem, this.budgetCategory);
  }

  public getPercentBeforeAction(budgetBalance: BudgetBalance): number {
    const percent: number = Math.round(budgetBalance.value / budgetBalance.completeValue * 100);
    return percent < 100 ? percent : 100;
  }

  public getActionPercent(budgetBalance: BudgetBalance): number {
    const previousPercent: number = this.getPercentBeforeAction(budgetBalance);
    const selectedPercent: number = Math.round(this.getActionValue(budgetBalance) / budgetBalance.completeValue * 100);
    return (selectedPercent + previousPercent) < 100 ? selectedPercent : (100 - previousPercent);
  }

  private getActionValue(budgetBalance: BudgetBalance): number {
    const value: number = this.historyItem.balance.value || 0;
    return budgetBalance.currency === this.historyItem.balance.currency ? value
      : CurrencyUtils.convertValueToCurrency(value, this.historyItem.balance.currency, budgetBalance.currency, this.historyItem.balance.alternativeCurrency);
  }
}
