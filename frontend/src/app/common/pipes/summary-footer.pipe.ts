import { Pipe, PipeTransform } from '@angular/core';

import {SummaryBalanceItem} from '../model/SummaryBalanceItem';
import {CurrencyService} from '../service/currency.service';
import {SummaryCategory} from '../model/SummaryCategory';
import {SummarySubCategory} from '../model/SummarySubCategory';

@Pipe({
  name: 'summaryFooter'
})
export class SummaryFooterPipe implements PipeTransform {

  public transform(categories: SummaryCategory[], currency: Currency): SummaryBalanceItem[] {
    const footerSummaries: SummaryBalanceItem[] = [];
    const balanceMap: Map<string, number> = new Map();

    categories.forEach((category: SummaryCategory) => {
      category.subCategories.forEach((subCategory: SummarySubCategory) => {
        subCategory.balance.forEach((balance: SummaryBalanceItem) => {
          const balanceCurrency: string = balance.currency;
          if (!balanceMap.has(balanceCurrency)) {
            balanceMap.set(balanceCurrency, balance.value);
          } else {
            balanceMap.set(balanceCurrency, balanceMap.get(balanceCurrency) + balance.value);
          }
        });
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
