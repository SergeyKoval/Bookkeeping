import { Pipe, PipeTransform } from '@angular/core';

import {CurrencyService} from '../../service/currency.service';

@Pipe({
  name: 'summaryBodySubAccount'
})
export class SummaryBodySubAccountPipe implements PipeTransform {

  public transform(items: BalanceItem[], currency: Currency): BalanceItem[] {
    if (!currency) {
      return items;
    }

    const balanceItem: BalanceItem = {currency: currency.name, value: 0};
    items.forEach((item: BalanceItem) => {
      balanceItem.value += CurrencyService.convertToCurrency(item.value, item.currency, currency);
    });

    return [balanceItem];
  }

}
