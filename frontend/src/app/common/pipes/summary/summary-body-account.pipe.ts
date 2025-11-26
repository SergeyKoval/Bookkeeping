import { Pipe, PipeTransform } from '@angular/core';

import { BaseSummaryPipe } from './baseSummaryPipe';
import { CurrencyService } from '../../service/currency.service';
import { FinAccount } from '../../model/fin-account';
import { CurrencyDetail } from '../../model/currency-detail';
import { BalanceItem } from '../../model/balance-item';

@Pipe({
    name: 'summaryBodyAccount',
    standalone: false
})
export class SummaryBodyAccountPipe extends BaseSummaryPipe implements PipeTransform {
  public constructor(protected currencyService: CurrencyService) {
    super(currencyService);
  }

  public transform(account: FinAccount, currency: CurrencyDetail): BalanceItem[] {
    const balanceMap: Map<string, number> = new Map();
    this.populateBalanceMap(account, balanceMap);
    return this.calculateBalance(balanceMap, currency);
  }
}
