import { Pipe, PipeTransform } from '@angular/core';

import {BaseSummaryPipe} from './baseSummaryPipe';

@Pipe({
  name: 'summaryBodyAccount'
})
export class SummaryBodyAccountPipe extends BaseSummaryPipe implements PipeTransform {

  public transform(account: FinAccount, currency: Currency): BalanceItem[] {
    const balanceMap: Map<string, number> = new Map();
    this.populateBalanceMap(account, balanceMap);
    return this.calculateBalance(balanceMap, currency);
  }
}
