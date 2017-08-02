import { Pipe, PipeTransform } from '@angular/core';

import {BaseSummaryPipe} from './baseSummaryPipe';

@Pipe({
  name: 'summaryFooter'
})
export class SummaryFooterPipe extends BaseSummaryPipe implements PipeTransform {

  public transform(accounts: FinAccount[], currency: Currency): BalanceItem[] {
    const balanceMap: Map<string, number> = new Map();

    accounts.forEach((account: FinAccount) => {
      this.populateBalanceMap(account, balanceMap);
    });

    return this.calculateBalance(balanceMap, currency);
  }
}
