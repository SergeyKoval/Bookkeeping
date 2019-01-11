import { Pipe, PipeTransform } from '@angular/core';

import { CategoryWrapper } from '../../model/budget/CategoryWrapper';
import { CloseMonthFilter } from '../../model/budget/CloseMonthFilter';

@Pipe({
  name: 'closeMonthCategoryFilter'
})
export class CloseMonthCategoryFilterPipe implements PipeTransform {

  public transform(categories: CategoryWrapper[], filters: CloseMonthFilter[]): CategoryWrapper[] {
    if (!filters || filters.length === 0) {
      return categories;
    }

    filters.forEach(filter => categories = CloseMonthCategoryFilterPipe.filter(categories, filter));
    return categories;
  }

  private static filter(categories: CategoryWrapper[], filter: CloseMonthFilter): CategoryWrapper[] {
    switch (filter) {
      case CloseMonthFilter.USED:
        return categories.filter(categoryWrapper => categoryWrapper.actionPlan);
      case CloseMonthFilter.UNUSED:
        return categories.filter(categoryWrapper => !categoryWrapper.actionPlan);
      case CloseMonthFilter.INCOME:
        return categories.filter(categoryWrapper => categoryWrapper.type === 'income');
      case CloseMonthFilter.EXPENSE:
        return categories.filter(categoryWrapper => categoryWrapper.type === 'expense');
    }
  }
}
