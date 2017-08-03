import {Inject, Injectable} from '@angular/core';
import {Http, Response} from '@angular/http';

import {Subject} from 'rxjs/Subject';
import {Observable} from 'rxjs/Observable';
import {ReplaySubject} from 'rxjs/ReplaySubject';

import {HOST} from '../config/config';
import {LoadingService} from './loading.service';
import {AssetImagePipe} from '../pipes/asset-image.pipe';

import 'rxjs/add/operator/do';

@Injectable()
export class SettingsService {
  private _accounts$$: Subject<FinAccount[]> = new ReplaySubject(1);
  private _categories$$: Subject<Category[]> = new ReplaySubject(1);

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
      .subscribe((response: Response) => this._accounts$$.next(response.json()));
  }

  public loadCategories(ownerId: number): void {
    this._loadingService.categories$$.next(true);
    this._http.get(`${this._host}/categories?ownerId=${ownerId}`)
      .delay(1500)
      .do(() => this._loadingService.categories$$.next(false))
      .subscribe((response: Response) => this._categories$$.next(response.json()));
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

  public transformCategories(categories: Category[]): SelectItem[] {
    const result: SelectItem[] = [];
    categories.forEach((category: Category) => {
      const subSelectItems: SelectItem[] = [];
      category.subCategories.forEach((subCategory: SubCategory) => {
        subSelectItems.push({title: subCategory.title});
      });
      const iconPath: string = category.icon ? this._assetImagePipe.transform(category.icon, 'category') : null;
      result.push({title: category.title, children: subSelectItems, icon: iconPath});
    });

    return result;
  }

  public get accounts$(): Observable<FinAccount[]> {
    return this._accounts$$;
  }


  public get categories$(): Observable<Category[]> {
    return this._categories$$;
  }
}
