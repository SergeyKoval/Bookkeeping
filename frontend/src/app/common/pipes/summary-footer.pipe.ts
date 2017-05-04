import { Pipe, PipeTransform } from '@angular/core';

import {SummaryBalanceItem} from '../model/SummaryBalanceItem';
import {CurrencyService} from '../service/currency.service';

@Pipe({
  name: 'summaryFooter'
})
export class SummaryFooterPipe implements PipeTransform {

  public transform(summaries: Summary[], currency: Currency): SummaryBalanceItem[] {
    const footerSummaries: SummaryBalanceItem[] = [];
    const balanceMap: Map<string, number> = new Map();

    summaries.forEach((summary: Summary) => {
      summary.balance.forEach((summaryBalance: SummaryBalance) => {
        const balanceCurrency: string = summaryBalance.currency;
        if (!balanceMap.has(balanceCurrency)) {
          balanceMap.set(balanceCurrency, summaryBalance.value);
        } else {
          balanceMap.set(balanceCurrency, balanceMap.get(balanceCurrency) + summaryBalance.value);
        }
      });
    });

    balanceMap.forEach((value: number, key: string) => {
      if (!currency) {
        footerSummaries.push(new SummaryBalanceItem(key, value));
      } else {
        if (footerSummaries.length === 0) {
          footerSummaries.push(new SummaryBalanceItem(currency.name, 0));
        }

        footerSummaries[0].value += CurrencyService.convertToCurrency(value, key, currency);
      }
    });

    return footerSummaries;
  }
}
