import {Inject, Injectable} from '@angular/core';
import {Http, Response} from '@angular/http';

import {Subject} from 'rxjs/Subject';
import {Subscription} from 'rxjs/Subscription';
import {Observable} from 'rxjs/Observable';

import {HOST} from '../config/config';
import {Category} from '../model/summary/Category';
import {SubCategory} from '../model/summary/SubCategory';
import {BalanceItem} from '../model/summary/BalanceItem';

import 'rxjs/add/operator/map';

@Injectable()
export class SummaryService {
  private _categories$$: Subject<Category[]> = new Subject();

  public constructor(
    private _http: Http,
    @Inject(HOST) private _host: string
  ) {}

  public loadSummaries(ownerId: number): void {
    const subscription: Subscription = this._http.get(`${this._host}/summaries?ownerId=${ownerId}`)
      .delay(1500)
      .map((response: Response) => response.json())
      .subscribe((summaries: Summary[]) => {
        const categoryMap: Map<string, Category> = new Map();

        summaries.forEach((summary: Summary) => {
          const categoryName: string = summary.category;
          if (!categoryMap.has(categoryName)) {
            categoryMap.set(categoryName, new Category(categoryName, summary.categoryOrder, summary.opened, []));
          }

          const category: Category = categoryMap.get(categoryName);
          const summarySubCategory: SubCategory = new SubCategory(summary.subCategory, summary.subCategoryOrder, []);
          category.subCategories.push(summarySubCategory);
          summary.balance.forEach((summaryBalance: SummaryBalance) => {
            summarySubCategory.balance.push(new BalanceItem(summaryBalance.currency, summaryBalance.value));
          });
        });

        const categories: Category[] = [];
        categoryMap.forEach((value: Category, key: string) => categories.push(value));
        this._categories$$.next(categories);
        subscription.unsubscribe();
      });
  }

  public get categories$(): Observable<Category[]> {
    return this._categories$$;
  }
}
