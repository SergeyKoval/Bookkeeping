import {Inject, Injectable} from '@angular/core';
import {Http, Response} from '@angular/http';

import {Subject} from 'rxjs/Subject';
import {Subscription} from 'rxjs/Subscription';
import {Observable} from 'rxjs/Observable';

import {HOST} from '../config/config';
import {SummaryCategory} from '../model/SummaryCategory';
import {SummarySubCategory} from '../model/SummarySubCategory';
import {SummaryBalanceItem} from '../model/SummaryBalanceItem';

import 'rxjs/add/operator/map';

@Injectable()
export class SummaryService {
  private _categories$$: Subject<SummaryCategory[]> = new Subject();

  public constructor(
    private _http: Http,
    @Inject(HOST) private _host: string
  ) {}

  public loadSummaries(ownerId: number): void {
    const subscription: Subscription = this._http.get(`${this._host}/summaries?ownerId=${ownerId}`)
      .delay(1500)
      .map((response: Response) => response.json())
      .subscribe((summaries: Summary[]) => {
        const categoryMap: Map<string, SummaryCategory> = new Map();

        summaries.forEach((summary: Summary) => {
          const categoryName: string = summary.category;
          if (!categoryMap.has(categoryName)) {
            categoryMap.set(categoryName, new SummaryCategory(categoryName, summary.categoryOrder, summary.opened, []));
          }

          const category: SummaryCategory = categoryMap.get(categoryName);
          const summarySubCategory: SummarySubCategory = new SummarySubCategory(summary.subCategory, summary.subCategoryOrder, []);
          category.subCategories.push(summarySubCategory);
          summary.balance.forEach((summaryBalance: SummaryBalance) => {
            summarySubCategory.balance.push(new SummaryBalanceItem(summaryBalance.currency, summaryBalance.value));
          });
        });

        const categories: SummaryCategory[] = [];
        categoryMap.forEach((value: SummaryCategory, key: string) => categories.push(value));
        this._categories$$.next(categories);
        subscription.unsubscribe();
      });
  }

  public get categories$(): Observable<SummaryCategory[]> {
    return this._categories$$;
  }
}
