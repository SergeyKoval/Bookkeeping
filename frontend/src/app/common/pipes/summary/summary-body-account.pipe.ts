import { Pipe, PipeTransform } from '@angular/core';

import { BaseSummaryPipe } from './baseSummaryPipe';
import { CurrencyService } from '../../service/currency.service';
import { Account } from '../../redux/reducers/user/account.reducer';

@Pipe({
  name: 'summaryBodyAccount'
})
export class SummaryBodyAccountPipe extends BaseSummaryPipe implements PipeTransform {
  public constructor(protected currencyService: CurrencyService) {
    super(currencyService);
  }

  public transform(account: Account, currency: CurrencyDetail): BalanceItem[] {
    const balanceMap: Map<string, number> = new Map();
    this.populateBalanceMap(account, balanceMap);
    return this.calculateBalance(balanceMap, currency);
  }
}
