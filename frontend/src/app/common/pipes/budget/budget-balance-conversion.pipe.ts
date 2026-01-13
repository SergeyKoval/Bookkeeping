import { Pipe, PipeTransform } from '@angular/core';

import { CurrencyService } from '../../service/currency.service';
import { CurrencyDetail } from '../../model/currency-detail';
import { BudgetBalance } from '../../model/budget/budget-balance';

export interface ConvertedBudgetBalance {
  currency: string;
  value: number;
  completeValue: number;
}

@Pipe({
    name: 'budgetBalanceConversion',
    standalone: false
})
export class BudgetBalanceConversionPipe implements PipeTransform {
  public constructor(private _currencyService: CurrencyService) {}

  public transform(balance: {[currency: string]: BudgetBalance}, conversionCurrency: CurrencyDetail, year: number, month: number): ConvertedBudgetBalance[] {
    if (!conversionCurrency) {
      const result: ConvertedBudgetBalance[] = [];
      Object.entries(balance)
        .forEach(([currency, budgetBalance]) => result.push({
          currency: currency,
          value: budgetBalance.value || 0,
          completeValue: budgetBalance.completeValue || 0
        }));
      return result;
    }

    const convertedBalance: ConvertedBudgetBalance = {
      currency: conversionCurrency.name,
      value: 0,
      completeValue: 0
    };

    Object.entries(balance).forEach(([currency, budgetBalance]) => {
      const value = budgetBalance.value || 0;
      const completeValue = budgetBalance.completeValue || 0;
      convertedBalance.value += this._currencyService.convertToAverageCurrency(
        value, currency, conversionCurrency.name, year, month
      );
      convertedBalance.completeValue += this._currencyService.convertToAverageCurrency(
        completeValue, currency, conversionCurrency.name, year, month
      );
    });

    return [convertedBalance];
  }
}
