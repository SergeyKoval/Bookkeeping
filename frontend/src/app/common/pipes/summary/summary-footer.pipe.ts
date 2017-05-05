import { Pipe, PipeTransform } from '@angular/core';

import {Category} from '../../model/summary/Category';
import {BalanceItem} from '../../model/summary/BalanceItem';
import {SubCategory} from '../../model/summary/SubCategory';
import {CurrencyService} from '../../service/currency.service';

@Pipe({
  name: 'summaryFooter'
})
export class SummaryFooterPipe implements PipeTransform {

  public transform(categories: Category[], currency: Currency): BalanceItem[] {
    const footerSummaries: BalanceItem[] = [];
    const balanceMap: Map<string, number> = new Map();

    categories.forEach((category: Category) => {
      category.subCategories.forEach((subCategory: SubCategory) => {
        subCategory.balance.forEach((balance: BalanceItem) => {
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
        footerSummaries.push(new BalanceItem(key, value));
      } else {
        if (footerSummaries.length === 0) {
          footerSummaries.push(new BalanceItem(currency.name, 0));
        }

        footerSummaries[0].value += CurrencyService.convertToCurrency(value, key, currency);
      }
    });

    return footerSummaries;
  }
}
