import { Pipe, PipeTransform } from '@angular/core';

import { BaseSummaryPipe } from './baseSummaryPipe';
import { CurrencyService } from '../../service/currency.service';
import { Account } from '../../redux/reducers/user/account.reducer';

@Pipe({
  name: 'summaryFooter'
})
export class SummaryFooterPipe extends BaseSummaryPipe implements PipeTransform {
  public constructor(protected currencyService: CurrencyService) {
    super(currencyService);
  }

  public transform(accounts: Array<Account>, currency: CurrencyDetail): BalanceItem[] {
    const balanceMap: Map<string, number> = new Map();

    accounts.forEach((account: FinAccount) => {
      this.populateBalanceMap(account, balanceMap);
    });

    return this.calculateBalance(balanceMap, currency);
  }
}
