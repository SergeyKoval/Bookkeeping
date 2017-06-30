import { Pipe, PipeTransform } from '@angular/core';

import {Account} from '../../model/summary/Account';
import {BalanceItem} from '../../model/summary/BalanceItem';
import {BaseSummaryPipe} from './baseSummaryPipe';


@Pipe({
  name: 'summaryBodyAccount'
})
export class SummaryBodyAccountPipe extends BaseSummaryPipe implements PipeTransform {

  public transform(account: Account, currency: Currency): BalanceItem[] {
    const balanceMap: Map<string, number> = new Map();
    this.populateBalanceMap(account, balanceMap);
    return this.calculateBalance(balanceMap, currency);
  }
}
