import { Pipe, PipeTransform } from '@angular/core';

import {SummaryBalanceItem} from '../model/SummaryBalanceItem';
import {CurrencyService} from '../service/currency.service';

@Pipe({
  name: 'summaryBalanceOrdering'
})
export class SummaryBalanceOrderingPipe implements PipeTransform {
  public constructor(private _currencyService: CurrencyService) {}

  public transform(items: SummaryBalanceItem[]): SummaryBalanceItem[] {
    if (items.length < 2) {
      return items;
    }

    return this._currencyService.sortSummaryBalanceItems(items);
  }
}
