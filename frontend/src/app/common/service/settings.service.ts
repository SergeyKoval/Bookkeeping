import { Inject, Injectable } from '@angular/core';
import { Http, Response } from '@angular/http';

import { Subject } from 'rxjs/Subject';
import { Observable } from 'rxjs/Observable';
import { ReplaySubject } from 'rxjs/ReplaySubject';

import { HOST } from '../config/config';
import { LoadingService } from './loading.service';
import { AssetImagePipe } from '../pipes/asset-image.pipe';

import 'rxjs/add/operator/do';

@Injectable()
export class SettingsService {
  private _accounts$$: Subject<FinAccount[]> = new ReplaySubject(1);
  private _categories$$: Subject<Category[]> = new ReplaySubject(1);
  private _categoryIcon: Map<string, string> = new Map();
  private _accountIcon: Map<string, string> = new Map();

  public constructor(
    private _http: Http,
    @Inject(HOST) private _host: string,
    private _loadingService: LoadingService,
    private _assetImagePipe: AssetImagePipe
  ) {}

  public loadAccounts(ownerId: number): void {
    this._loadingService.accounts$$.next(true);
    this._http.get(`${this._host}/accounts?ownerId=${ownerId}`)
      .delay(1500)
      .do(() => this._loadingService.accounts$$.next(false))
      .subscribe((response: Response) => {
        const accounts: FinAccount[] = response.json();
        this._accountIcon.clear();
        accounts.forEach((account: FinAccount) => {
          account.subAccounts.forEach((subAccount: SubAccount) => this._accountIcon.set(`${account.title}-${subAccount.title}`, subAccount.icon));
        });
        this._accounts$$.next(accounts);
      });
  }

  public loadCategories(ownerId: number): void {
    this._loadingService.categories$$.next(true);
    this._http.get(`${this._host}/categories?ownerId=${ownerId}`)
      .delay(1500)
      .do(() => this._loadingService.categories$$.next(false))
      .subscribe((response: Response) => {
        const categories: Category[] = response.json();
        this._categoryIcon.clear();
        categories.forEach((category: Category) => this._categoryIcon.set(category.title, category.icon));
        this._categories$$.next(categories);
      });
  }

  public transformAccounts(accounts: FinAccount[]): SelectItem[] {
    const result: SelectItem[] = [];
    accounts.forEach((account: FinAccount) => {
      const subSelectItems: SelectItem[] = [];
      account.subAccounts.forEach((subAccount: SubAccount) => {
        const iconPath: string = subAccount.icon ? this._assetImagePipe.transform(subAccount.icon, 'account') : null;
        subSelectItems.push({title: subAccount.title, icon: iconPath});
      });
      result.push({title: account.title, children: subSelectItems});
    });

    return result;
  }

  public transformCategories(categories: Category[], type: string): SelectItem[] {
    const result: SelectItem[] = [];
    categories.forEach((category: Category) => {
      const subSelectItems: SelectItem[] = [];
      category.subCategories.forEach((subCategory: SubCategory) => {
        if (!type || type === subCategory.type) {
          subSelectItems.push({title: subCategory.title});
        }
      });
      if (subSelectItems.length > 0) {
        const iconPath: string = category.icon ? this._assetImagePipe.transform(category.icon, 'category') : null;
        result.push({title: category.title, children: subSelectItems, icon: iconPath});
      }
    });

    return result;
  }

  public static chooseSelectedItem(items: SelectItem[], firstLevel: string, secondLevel: string): SelectItem[] {
    const selectedItem: SelectItem[] = [];
    items.forEach((item: SelectItem) => {
      if (item.title === firstLevel) {
        selectedItem.push(item);
        item.children.forEach((subItem: SelectItem) => {
          if (subItem.title === secondLevel) {
            selectedItem.push(subItem);
          }
        });
      }
    });

    return selectedItem;
  }

  public getCategoryIcon(categoryTitle: string): string {
    return this._categoryIcon.get(categoryTitle);
  }

  public getAccountIcon(account: string, subAccount: string): string {
    return this._accountIcon.get(`${account}-${subAccount}`);
  }

  public get accounts$(): Observable<FinAccount[]> {
    return this._accounts$$;
  }


  public get categories$(): Observable<Category[]> {
    return this._categories$$;
  }
}
