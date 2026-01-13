import { Pipe, PipeTransform } from '@angular/core';

import { CurrencyService } from '../../service/currency.service';
import { CurrencyDetail } from '../../model/currency-detail';
import { BudgetBalance } from '../../model/budget/budget-balance';

export interface ConvertedGoalBalance {
  currency: string;
  value: number;
  completeValue: number;
}

@Pipe({
    name: 'budgetGoalBalanceConversion',
    standalone: false
})
export class BudgetGoalBalanceConversionPipe implements PipeTransform {
  public constructor(private _currencyService: CurrencyService) {}

  public transform(balance: BudgetBalance, conversionCurrency: CurrencyDetail, year: number, month: number): ConvertedGoalBalance {
    const value = balance.value || 0;
    const completeValue = balance.completeValue || 0;

    if (!conversionCurrency) {
      return {
        currency: balance.currency,
        value: value,
        completeValue: completeValue
      };
    }

    return {
      currency: conversionCurrency.name,
      value: this._currencyService.convertToAverageCurrency(
        value, balance.currency, conversionCurrency.name, year, month
      ),
      completeValue: this._currencyService.convertToAverageCurrency(
        completeValue, balance.currency, conversionCurrency.name, year, month
      )
    };
  }
}
