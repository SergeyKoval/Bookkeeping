import { Inject, Injectable } from '@angular/core';
import { AbstractControl, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { ActivatedRouteSnapshot, CanActivate, Router, RouterStateSnapshot } from '@angular/router';

import { Subject ,  ReplaySubject ,  Observable } from 'rxjs';
import { delay, tap } from 'rxjs/operators';
import { isNullOrUndefined } from 'util';

import { LoadingService } from 'app/common/service/loading.service';
import { HOST } from '../config/config';
import { AssetImagePipe } from '../pipes/asset-image.pipe';

@Injectable()
export class ProfileService implements CanActivate {
  private _authenticatedProfile: Profile;
  private _userCurrencies: Map<String, CurrencyDetail> = new Map();
  private _categoryIcon: Map<string, string> = new Map();
  private _accountIcon: Map<string, string> = new Map();
  private _accounts$$: Subject<FinAccount[]> = new ReplaySubject(1);

  public constructor(
    private _router: Router,
    private _formBuilder: FormBuilder,
    private _loadingService: LoadingService,
    private _assetImagePipe: AssetImagePipe,
    private _http: HttpClient,
    @Inject(HOST) private _host: string
  ) {}

  public canActivate(route: ActivatedRouteSnapshot, state: RouterStateSnapshot): boolean {
    if (!isNullOrUndefined(this._authenticatedProfile)) {
      return true;
    }

    this._router.navigate(['/authentication']);
    return false;
  }

  public loadFullProfile(): Observable<Profile> {
    return this._http.get<Profile>('/api/profile/full')
      .pipe(
        tap(profile => {
          profile.currencies.sort((first: CurrencyDetail, second: CurrencyDetail) => first.order - second.order);
          profile.currencies.forEach((currency: CurrencyDetail) => this._userCurrencies.set(currency.name, currency));
          profile.categories.forEach((category: Category) => this._categoryIcon.set(category.title, category.icon));
          profile.accounts.forEach((account: FinAccount) => {
            account.subAccounts.forEach((subAccount: SubAccount) => this._accountIcon.set(`${account.title}-${subAccount.title}`, subAccount.icon));
          });
          this._accounts$$.next(profile.accounts);
          this._authenticatedProfile = profile;
      }));
  }

  public clearProfile(): void {
    this._authenticatedProfile = null;
    this._userCurrencies.clear();
    this._categoryIcon.clear();
    this._accountIcon.clear();
    this._accounts$$.next(null);
  }

  public isProfileLoaded(): boolean {
    return !isNullOrUndefined(this.authenticatedProfile);
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
    let defaultCurrency: CurrencyDetail = null;
    this._authenticatedProfile.currencies.forEach((currency: CurrencyDetail) => {
      if (currency.default) {
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

  public reloadAccounts(): void {
    this._loadingService.accounts$$.next(true);
    this._http.get<FinAccount[]>(`${this._host}/profiles/${this.authenticatedProfile.id}/accounts`)
      .pipe(
        delay(3000),
        tap(() => this._loadingService.accounts$$.next(false))
      ).subscribe((accounts: FinAccount[]) => {
        this._accountIcon.clear();
        accounts.forEach((account: FinAccount) => {
          account.subAccounts.forEach((subAccount: SubAccount) => this._accountIcon.set(`${account.title}-${subAccount.title}`, subAccount.icon));
        });
        this._accounts$$.next(accounts);
      });
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

  public prepareProfileForm(): FormGroup {
    return this._formBuilder.group({
      id: [this._authenticatedProfile.id],
      email: this._formBuilder.control({value: this._authenticatedProfile.email, disabled: true}),
      oldPassword: ['', Validators.required],
      newPassword: this._formBuilder.control({value: '', disabled: true}, Validators.required),
      newPasswordAgain: this._formBuilder.control({value: '', disabled: true}, Validators.required)
    }, {
      validator: ProfileService.validateNewPassword
    });
  }

  public reloadProfile(): Observable<Profile[]> {
    return this._http.get<Profile[]>(`${this._host}/profiles?email=${this._authenticatedProfile.email}`, {headers: new HttpHeaders({'Cache-Control': 'no-cache'})})
      .pipe(
        delay(3000),
        tap((profiles: Profile[]) => this._authenticatedProfile = profiles[0]),
        tap(x => this._authenticatedProfile.currencies.push({'name': 'EUR', 'default': true, 'symbol': '&euro;', 'order': 3})),
        tap(x => this._authenticatedProfile.currencies.forEach((currency: CurrencyDetail) => this._userCurrencies.set(currency.name, currency))));
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

    if (!fg.invalid && passwordAgainValue.length >= 1 && passwordAgainValue !== passwordValue) {
      newPasswordAgainFormControl.setErrors({message: 'Новый пароль введен второй раз неверно'});
    }
  }
}
