import { Pipe, PipeTransform } from '@angular/core';

import { CurrencyService } from '../../service/currency.service';

@Pipe({
  name: 'summaryBodySubAccount'
})
export class SummaryBodySubAccountPipe implements PipeTransform {
  public constructor(private _currencyService: CurrencyService) {}

  public transform(items: BalanceItem[], currency: CurrencyDetail): BalanceItem[] {
    if (!currency) {
      return items;
    }

    const balanceItem: BalanceItem = {currency: currency.name, value: 0};
    items.forEach((item: BalanceItem) => {
      balanceItem.value += this._currencyService.convertToCurrency(item.value, item.currency, currency);
    });

    return [balanceItem];
  }

}
