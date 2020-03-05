import { Injectable } from '@angular/core';
import { AbstractControl, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { HttpClient, HttpResponse } from '@angular/common/http';
import { ActivatedRouteSnapshot, CanActivate, Router, RouterStateSnapshot } from '@angular/router';

import { Observable, of, ReplaySubject, Subject } from 'rxjs';
import { catchError, filter, map, tap } from 'rxjs/operators';
import { switchMap } from 'rxjs/internal/operators';

import { LoadingService } from 'app/common/service/loading.service';
import { AssetImagePipe } from '../pipes/asset-image.pipe';
import { CurrencyService } from './currency.service';
import { AlertService } from './alert.service';
import { AlertType } from '../model/alert/AlertType';

@Injectable()
export class ProfileService implements CanActivate {
  private _observableProfile: Observable<Profile>;
  private _authenticatedProfile: Profile;
  private _userCurrencies: Map<String, CurrencyDetail> = new Map();
  private _categoryIcon: Map<string, string> = new Map();
  private _accountIcon: Map<string, string> = new Map();
  private _accounts$$: Subject<FinAccount[]> = new ReplaySubject(1);
  private _initialDataLoaded: boolean = false;

  public constructor(
    private _alertService: AlertService,
    private _router: Router,
    private _formBuilder: FormBuilder,
    private _loadingService: LoadingService,
    private _currencyService: CurrencyService,
    private _assetImagePipe: AssetImagePipe,
    private _http: HttpClient
  ) {}

  public canActivate(route: ActivatedRouteSnapshot, state: RouterStateSnapshot): Observable<boolean> {
    if (this._authenticatedProfile) {
      return of(this._authenticatedProfile.roles.indexOf('ADMIN') > -1);
    }

    const result: Subject<boolean> = new Subject<boolean>();
    this._observableProfile.subscribe(profile => result.next(profile.roles.indexOf('ADMIN') > -1));

    return result.asObservable();
  }

  public loadFullProfile(): Observable<Profile> {
    this._observableProfile = this.getUserProfile();
    return this._observableProfile
      .pipe(
        tap(profile => {
          profile.currencies.sort((first: CurrencyDetail, second: CurrencyDetail) => first.order - second.order);
          profile.accounts.sort((first: FinAccount, second: FinAccount) => first.order - second.order);
          profile.categories.sort((first: Category, second: Category) => first.order - second.order);
          profile.currencies.forEach((currency: CurrencyDetail) => this._userCurrencies.set(currency.name, currency));
          profile.categories.forEach((category: Category) => {
            category.subCategories.sort((first: SubCategory, second: SubCategory) => first.order - second.order);
            this._categoryIcon.set(category.title, category.icon);
          });
          profile.accounts.forEach((account: FinAccount) => {
            account.subAccounts.sort((first: SubAccount, second: SubAccount) => first.order - second.order);
            account.subAccounts.forEach((subAccount: SubAccount) => this._accountIcon.set(`${account.title}-${subAccount.title}`, subAccount.icon));
          });
          this._accounts$$.next(profile.accounts);
          this._authenticatedProfile = profile;
      }));
  }

  public reloadCurrenciesAndAccountsInProfile(): Observable<CurrencyHistory[]> {
    this._userCurrencies.clear();
    this._currencyService.clearCurrencies();

    return this.getUserProfile()
      .pipe(
        tap(profile => {
          profile.currencies.sort((first: CurrencyDetail, second: CurrencyDetail) => first.order - second.order);
          profile.accounts.sort((first: FinAccount, second: FinAccount) => first.order - second.order);
          profile.accounts.forEach((account: FinAccount) => {
            account.subAccounts.sort((first: SubAccount, second: SubAccount) => first.order - second.order);
          });
          profile.currencies.forEach((currency: CurrencyDetail) => this._userCurrencies.set(currency.name, currency));
          this.authenticatedProfile.currencies = profile.currencies;
          this.authenticatedProfile.accounts = profile.accounts;
          this._accounts$$.next(profile.accounts);
        }),
        switchMap(() => this._currencyService.loadCurrenciesForCurrentMoth(this.getProfileCurrencies()))
      );
  }

  public reloadAccountsInProfile(): Observable<Profile> {
    this._accountIcon.clear();
    return this.getUserProfile()
      .pipe(
        tap(profile => {
          profile.accounts.sort((first: FinAccount, second: FinAccount) => first.order - second.order);
          profile.accounts.forEach((account: FinAccount) => {
            account.subAccounts.sort((first: SubAccount, second: SubAccount) => first.order - second.order);
            const finAccount: FinAccount = this.authenticatedProfile.accounts.filter(oldAccount => oldAccount.title === account.title)[0];
            account.settingsOpened = finAccount ? finAccount.settingsOpened : false;
            account.subAccounts.forEach((subAccount: SubAccount) => this._accountIcon.set(`${account.title}-${subAccount.title}`, subAccount.icon));
          });
          this.authenticatedProfile.accounts = profile.accounts;
          this._accounts$$.next(profile.accounts);
        }));
  }

  public quiteReloadAccounts(): void {
    this._loadingService.accounts$$.next(true);
    this.reloadAccountsInProfile().subscribe(() => this._loadingService.accounts$$.next(false));
  }

  public reloadCategoriesInProfile(): Observable<Profile> {
    this._categoryIcon.clear();
    return this.getUserProfile()
      .pipe(
        tap(profile => {
          profile.categories.sort((first: Category, second: Category) => first.order - second.order);
          profile.categories.forEach((category: Category) => {
            this._categoryIcon.set(category.title, category.icon);
            category.subCategories.sort((first: SubCategory, second: SubCategory) => first.order - second.order);
            const userCategory: Category = this.authenticatedProfile.categories.filter(oldCategory => oldCategory.title === category.title)[0];
            category.opened = userCategory ? userCategory.opened : false;
          });
          this._authenticatedProfile.categories = profile.categories;
        }));
  }

  public reloadDevicesInProfile(): Observable<Profile> {
    return this.getUserProfile().pipe(tap(profile => this.authenticatedProfile.devices = profile.devices));
  }

  public clearProfile(): void {
    this._authenticatedProfile = null;
    this._userCurrencies.clear();
    this._categoryIcon.clear();
    this._accountIcon.clear();
    this._accounts$$.next([]);
  }

  public updatePassword(profile: {oldPassword: string, newPassword: string}): Observable<SimpleResponse> {
    return this._http.post<SimpleResponse>('/api/profile/change-password', profile);
  }

  public updateProfileUseCurrency(currencyName: string): Observable<SimpleResponse> {
    return this._http.post<SimpleResponse>('/api/profile/update-user-currency', {name: currencyName, use: true});
  }

  public updateProfileUnUseCurrency(currencyName: string): Observable<SimpleResponse> {
    return this._http.post<SimpleResponse>('/api/profile/update-user-currency', {name: currencyName, use: false});
  }

  public markCurrencyAsDefault(currencyName: string): Observable<SimpleResponse> {
    return this._http.post<SimpleResponse>('/api/profile/update-user-currency-default', {name: currencyName});
  }

  public moveCurrencyUp(currencyName: string): Observable<SimpleResponse> {
    return this._http.post<SimpleResponse>('/api/profile/update-user-currency-move', {name: currencyName, direction: 'UP'});
  }

  public moveCurrencyDown(currencyName: string): Observable<SimpleResponse> {
    return this._http.post<SimpleResponse>('/api/profile/update-user-currency-move', {name: currencyName, direction: 'DOWN'});
  }

  public addAccount(accountTitle: string): Observable<SimpleResponse> {
    return this._http.post<SimpleResponse>('/api/profile/add-account', {title: accountTitle});
  }

  public editAccount(oldAccountTitle: string, newAccountTitle: string): Observable<SimpleResponse> {
    return this._http.post<SimpleResponse>('/api/profile/edit-account', {title: newAccountTitle, oldTitle: oldAccountTitle});
  }

  public deleteAccount(accountTitle: string): Observable<SimpleResponse> {
    return this._http.post<SimpleResponse>('/api/profile/delete-account', {title: accountTitle});
  }

  public moveAccountDown(accountTitle: string): Observable<SimpleResponse> {
    return this._http.post<SimpleResponse>('/api/profile/move-account', {title: accountTitle, direction: 'DOWN'});
  }

  public moveAccountUp(accountTitle: string): Observable<SimpleResponse> {
    return this._http.post<SimpleResponse>('/api/profile/move-account', {title: accountTitle, direction: 'UP'});
  }

  public addSubAccount(subAccountTitle: string, accountTitle: string, icon: string, balance: {[currency: string]: number}): Observable<SimpleResponse> {
    return this._http.post<SimpleResponse>('/api/profile/add-sub-account', {
      'title': subAccountTitle,
      'parentTitle': accountTitle,
      'icon': icon,
      'balance': balance
    });
  }

  public changeSubAccountBalance(accountTitle: string, subAccountTitle: string, balance: {[currency: string]: number}): Observable<SimpleResponse> {
    return this._http.post<SimpleResponse>('/api/profile/change-sub-account-balance', {
      'title': subAccountTitle,
      'parentTitle': accountTitle,
      'balance': balance
    });
  }

  public editSubAccount(accountTitle: string, oldSubAccountTitle: string, newSubAccountTitle: string, icon: string, balance: {[currency: string]: number}): Observable<SimpleResponse> {
    return this._http.post<SimpleResponse>('/api/profile/edit-sub-account', {
      'oldTitle': oldSubAccountTitle,
      'title': newSubAccountTitle,
      'parentTitle': accountTitle,
      'balance': balance,
      'icon': icon
    });
  }

  public moveSubAccountUp(accountTitle: string, subAccountTitle: string): Observable<SimpleResponse> {
    return this._http.post<SimpleResponse>('/api/profile/move-sub-account', {title: subAccountTitle, parentTitle: accountTitle, direction: 'UP'});
  }

  public moveSubAccountDown(accountTitle: string, subAccountTitle: string): Observable<SimpleResponse> {
    return this._http.post<SimpleResponse>('/api/profile/move-sub-account', {title: subAccountTitle, parentTitle: accountTitle, direction: 'DOWN'});
  }

  public toggleAccount(accountTitle: string, toggleState: boolean): void {
    this._http.post<SimpleResponse>('/api/profile/toggle-account', {'title': accountTitle, 'toggleState': toggleState})
      .subscribe(simpleResponse => {
        if (simpleResponse.status === 'FAIL') {
          this._alertService.addAlert(AlertType.WARNING, 'Ошибка при сохранения измененного статуса счета');
        }
      });
  }

  public deleteSubAccount(accountTitle: string, subAccountTitle: string): Observable<SimpleResponse> {
    return this._http.post<SimpleResponse>('/api/profile/delete-sub-account', {title: subAccountTitle, parentTitle: accountTitle});
  }

  public addCategory(categoryTitle: string, icon: string): Observable<SimpleResponse> {
    return this._http.post<SimpleResponse>('/api/profile/add-category', {'title': categoryTitle, 'icon': icon});
  }

  public editCategory(oldCategoryTitle: string, newCategoryTitle: string, icon: string): Observable<SimpleResponse> {
    return this._http.post<SimpleResponse>('/api/profile/edit-category', {'title': newCategoryTitle, 'oldTitle': oldCategoryTitle, 'icon': icon});
  }

  public deleteCategory(categoryTitle: string): Observable<SimpleResponse> {
    return this._http.post<SimpleResponse>('/api/profile/delete-category', {title: categoryTitle});
  }

  public moveCategoryUp(categoryTitle: string): Observable<SimpleResponse> {
    return this._http.post<SimpleResponse>('/api/profile/move-category', {title: categoryTitle, direction: 'UP'});
  }

  public moveCategoryDown(categoryTitle: string): Observable<SimpleResponse> {
    return this._http.post<SimpleResponse>('/api/profile/move-category', {title: categoryTitle, direction: 'DOWN'});
  }

  public addSubCategory(subCategoryTitle: string, categoryTitle: string, subCategoryType: string): Observable<SimpleResponse> {
    return this._http.post<SimpleResponse>('/api/profile/add-sub-category', {'title': subCategoryTitle, 'parentTitle': categoryTitle, 'subCategoryType': subCategoryType});
  }

  public editSubCategory(categoryTitle: string, oldSubCategoryTitle: string, newSubCategoryTitle: string, subCategoryType: string): Observable<SimpleResponse> {
    return this._http.post<SimpleResponse>('/api/profile/edit-sub-category', {
      'oldTitle': oldSubCategoryTitle,
      'title': newSubCategoryTitle,
      'parentTitle': categoryTitle,
      'subCategoryType': subCategoryType
    });
  }

  public deleteSubCategory(categoryTitle: string, subCategoryTitle: string, subCategoryType: string): Observable<SimpleResponse> {
    return this._http.post<SimpleResponse>('/api/profile/delete-sub-category', {'title': subCategoryTitle, 'parentTitle': categoryTitle, 'subCategoryType': subCategoryType});
  }

  public moveSubCategoryUp(categoryTitle: string, subCategoryTitle: string, subCategoryType: string): Observable<SimpleResponse> {
    return this._http.post<SimpleResponse>('/api/profile/move-sub-category', {'title': subCategoryTitle, 'parentTitle': categoryTitle, 'subCategoryType': subCategoryType, 'direction': 'UP'});
  }

  public moveSubCategoryDown(categoryTitle: string, subCategoryTitle: string, subCategoryType: string): Observable<SimpleResponse> {
    return this._http.post<SimpleResponse>('/api/profile/move-sub-category', {'title': subCategoryTitle, 'parentTitle': categoryTitle, 'subCategoryType': subCategoryType, 'direction': 'DOWN'});
  }

  public moveCategory(oldCategoryTitle: string, newCategoryTitle: string, subCategoryTitle: string, type: string): Observable<SimpleResponse> {
    return this._http.post<SimpleResponse>('/api/profile/move-sub-category-to-another-category', {
      'oldTitle': oldCategoryTitle,
      'parentTitle': newCategoryTitle,
      'title': subCategoryTitle,
      'subCategoryType': type
    });
  }

  public get authenticatedProfile(): Profile {
    return this._authenticatedProfile;
  }

  public getCurrencyDetails(currency: string): CurrencyDetail {
    return this._userCurrencies.get(currency);
  }

  public isCurrencyUsed(currency: string): boolean {
    return this._userCurrencies.has(currency);
  }

  public getProfileCurrencies(): string[] {
    return this.authenticatedProfile.currencies.map((currency: CurrencyDetail) => currency.name);
  }

  public get defaultCurrency(): CurrencyDetail {
    if (!this._authenticatedProfile) {
      console.error('Accessing profile of the unauthorized user');
      return {};
    }

    let defaultCurrency: CurrencyDetail = null;
    this._authenticatedProfile.currencies.forEach((currency: CurrencyDetail) => {
      if (currency.defaultCurrency) {
        defaultCurrency = currency;
        return;
      }
    });

    return defaultCurrency || this._authenticatedProfile.currencies.values().next().value;
  }

  public getCategoryIcon(categoryTitle: string): string {
    return this._categoryIcon.get(categoryTitle);
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

  public getAccountIcon(account: string, subAccount: string): string {
    return this._accountIcon.get(`${account}-${subAccount}`);
  }

  public get accounts$(): Observable<FinAccount[]> {
    return this._accounts$$;
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

  public sortSummaryBalanceItems(items: BalanceItem[]): BalanceItem[] {
    items.sort((firstItem: BalanceItem, secondItem: BalanceItem) => {
      const firstCurrency: CurrencyDetail = this.getCurrencyDetails(firstItem.currency);
      const secondCurrency: CurrencyDetail = this.getCurrencyDetails(secondItem.currency);
      return (firstCurrency ? firstCurrency.order : Number.MAX_VALUE) - (secondCurrency ? secondCurrency.order : Number.MAX_VALUE);
    });
    return items;
  }

  public prepareProfileForm(): FormGroup {
    return this._formBuilder.group({
      email: this._formBuilder.control({value: this._authenticatedProfile.email, disabled: true}),
      oldPassword: ['', Validators.required],
      newPassword: this._formBuilder.control({value: '', disabled: true}, [Validators.required, Validators.minLength(3)]),
      newPasswordAgain: this._formBuilder.control({value: '', disabled: true}, [Validators.required, Validators.minLength(3)])
    }, {
      validator: ProfileService.validateNewPassword
    });
  }

  public downloadAndroidApplication(): Observable<Blob> {
    return this._http.get(`/mobile-app/android`, {observe: 'response', responseType: 'blob'})
      .pipe(
        filter((response: HttpResponse<Blob>) => response.status === 200),
        map((response: HttpResponse<Blob>) => response.body),
        catchError(() => {
          this._alertService.addAlert(AlertType.DANGER, `Error loading`);
          return of(null);
        })
      );
  }

  public sendApplicationEmail (email: {}): Observable<SimpleResponse> {
    return this._http.post<SimpleResponse>('/api/profile/send-application-link', email);
  }

  public changeDeviceName(deviceDetails: {}): Observable<SimpleResponse> {
    return this._http.post<SimpleResponse>('/api/profile/change-device-name', deviceDetails);
  }

  public getDeviceSms(deviceId: string, smsIndex: number): Observable<SimpleResponse> {
    return this._http.get<SimpleResponse>(`/api/history/devices/${deviceId}/sms/${smsIndex}`);
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

  public static validateNewPassword(fg: FormGroup): void {
    const newPasswordAgainFormControl: AbstractControl = fg.controls.newPasswordAgain;
    const passwordAgainValue: string = newPasswordAgainFormControl.value;
    const passwordValue: string = fg.controls.newPassword.value;

    if (!fg.invalid && passwordAgainValue && passwordAgainValue.length >= 1) {
      if (passwordValue.length < 3) {
        newPasswordAgainFormControl.setErrors({message: 'Минимальная длина 3 символа'});
      } else if (passwordAgainValue !== passwordValue) {
        newPasswordAgainFormControl.setErrors({message: 'Новый пароль введен второй раз неверно'});
      }
    }
  }

  public set initialDataLoaded(value: boolean) {
    this._initialDataLoaded = value;
  }

  public get initialDataLoaded(): boolean {
    return this._initialDataLoaded;
  }

  private getUserProfile(): Observable<Profile> {
    return this._http.get<Profile>('/api/profile/full');
  }
}
