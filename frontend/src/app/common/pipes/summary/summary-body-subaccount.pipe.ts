import { Pipe, PipeTransform } from '@angular/core';

import { CurrencyService } from '../../service/currency.service';

@Pipe({
  name: 'summaryBodySubAccount'
})
export class SummaryBodySubAccountPipe implements PipeTransform {
  public constructor(private _currencyService: CurrencyService) {}

  public transform(balance: {[currency: string]: number}, currency: CurrencyDetail): BalanceItem[] {
    if (!currency) {
      const result: BalanceItem[] = [];
      Object.entries(balance).forEach(([balanceCurrency, balanceValue]) => result.push({currency: balanceCurrency, value: balanceValue}));
      return result;
    }

    const balanceItem: BalanceItem = {currency: currency.name, value: 0};
    Object.entries(balance).forEach(([balanceCurrency, balanceValue]) => {
      balanceItem.value += this._currencyService.convertToCurrency(balanceValue, balanceCurrency, currency.name);
    });
    return [balanceItem];
  }
}
