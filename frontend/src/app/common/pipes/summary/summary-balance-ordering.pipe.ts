import { Pipe, PipeTransform } from '@angular/core';

import {CurrencyService} from '../../service/currency.service';

@Pipe({
  name: 'summaryBalanceOrdering'
})
export class SummaryBalanceOrderingPipe implements PipeTransform {
  public constructor(private _currencyService: CurrencyService) {}

  public transform(items: BalanceItem[]): BalanceItem[] {
    if (items.length < 2) {
      return items;
    }

    return this._currencyService.sortSummaryBalanceItems(items);
  }
}
