import { Pipe, PipeTransform } from '@angular/core';

import {Category} from '../../model/summary/Category';
import {BalanceItem} from '../../model/summary/BalanceItem';
import {BaseSummaryPipe} from './baseSummaryPipe';


@Pipe({
  name: 'summaryBodyCategory'
})
export class SummaryBodyCategoryPipe extends BaseSummaryPipe implements PipeTransform {

  public transform(category: Category, currency: Currency): BalanceItem[] {
    const balanceMap: Map<string, number> = new Map();
    this.populateBalanceMap(category, balanceMap);
    return this.calculateBalance(balanceMap, currency);
  }
}
