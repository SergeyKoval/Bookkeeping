import { Pipe, PipeTransform } from '@angular/core';
import { ProfileService } from '../service/profile.service';
import { BudgetCategory } from '../model/budget/budget-category';

@Pipe({
    name: 'categorySort',
    standalone: false
})
export class CategorySortPipe implements PipeTransform {

  public constructor(private _profileService: ProfileService) {}

  public transform(categories: BudgetCategory[]): BudgetCategory[] {
    const categoryOrder: Map<string, number>  = new Map<string, number>();
    this._profileService.authenticatedProfile.categories.forEach(category => categoryOrder.set(category.title, category.order));

    categories.sort((a: BudgetCategory, b: BudgetCategory) => {
      if (!categoryOrder.has(a.title)) {
        return 1;
      }
      if (!categoryOrder.has(b.title)) {
        return -1;
      }
      return categoryOrder.get(a.title) - categoryOrder.get(b.title);
    });

    return categories;
  }
}
