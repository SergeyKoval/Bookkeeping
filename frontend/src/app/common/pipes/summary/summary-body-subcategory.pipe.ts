import { Pipe, PipeTransform } from '@angular/core';

import {BalanceItem} from '../../model/summary/BalanceItem';
import {CurrencyService} from '../../service/currency.service';

@Pipe({
  name: 'summaryBodySubcategory'
})
export class SummaryBodySubcategoryPipe implements PipeTransform {

  public transform(items: BalanceItem[], currency: Currency): BalanceItem[] {
    if (!currency) {
      return items;
    }

    const balanceItem: BalanceItem = new BalanceItem(currency.name, 0);
    items.forEach((item: BalanceItem) => {
      balanceItem.value += CurrencyService.convertToCurrency(item.value, item.currency, currency);
    });

    return [balanceItem];
  }

}
