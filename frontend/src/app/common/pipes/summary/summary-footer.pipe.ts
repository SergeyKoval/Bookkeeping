import { Pipe, PipeTransform } from '@angular/core';

import {Account} from '../../model/summary/Account';
import {BalanceItem} from '../../model/summary/BalanceItem';
import {BaseSummaryPipe} from './baseSummaryPipe';

@Pipe({
  name: 'summaryFooter'
})
export class SummaryFooterPipe extends BaseSummaryPipe implements PipeTransform {

  public transform(accounts: Account[], currency: Currency): BalanceItem[] {
    const balanceMap: Map<string, number> = new Map();

    accounts.forEach((account: Account) => {
      this.populateBalanceMap(account, balanceMap);
    });

    return this.calculateBalance(balanceMap, currency);
  }
}
