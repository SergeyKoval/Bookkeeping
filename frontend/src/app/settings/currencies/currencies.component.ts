import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';

import { filter, switchMap, tap } from 'rxjs/operators';
import { Observable, Subject } from 'rxjs/index';

import { CurrencyService } from '../../common/service/currency.service';
import { ProfileService } from '../../common/service/profile.service';
import { ConfirmDialogService } from '../../common/components/confirm-dialog/confirm-dialog.service';
import { AlertService } from '../../common/service/alert.service';
import { AlertType } from '../../common/model/alert/AlertType';
import { LoadingService } from '../../common/service/loading.service';

@Component({
  selector: 'bk-currencies',
  templateUrl: './currencies.component.html',
  styleUrls: ['./currencies.component.css']
})
export class CurrenciesComponent implements OnInit {
  public loading: boolean = true;
  public allCurrencies: CurrencyDetail[];

  private _ACCOUNTS_LOADING: Subject<boolean>;

  public constructor(
    private _router: Router,
    private _loadingService: LoadingService,
    private _alertService: AlertService,
    private _confirmDialogService: ConfirmDialogService,
    private _currencyService: CurrencyService,
    private _profileService: ProfileService
  ) {}

  public ngOnInit(): void {
    this._ACCOUNTS_LOADING = this._loadingService.accounts$$;
    this.init();
  }

  public isCurrencyUsed(currencyName: string): boolean {
    return this._profileService.isCurrencyUsed(currencyName);
  }

  public isCurrencyDefault(currencyName: string): boolean {
    const currencyDetails: CurrencyDetail = this._profileService.getCurrencyDetails(currencyName);
    return currencyDetails ? currencyDetails.defaultCurrency : false;
  }

  public useCurrencyForProfile(currencyName: string): void {
    this.loading = true;
    this._ACCOUNTS_LOADING.next(true);
    this._profileService.updateProfileUseCurrency(currencyName)
      .pipe(
        tap(simpleResponse => {
          if (simpleResponse.status === 'FAIL') {
            this._alertService.addAlert(AlertType.WARNING, 'Во время сохранения произошла ошибка');
          } else {
            this._alertService.addAlert(AlertType.SUCCESS, 'Валюта успешно добавлена');
          }
        }),
        switchMap(() => this._profileService.reloadCurrenciesAndAccountsInProfile())
      ).subscribe(() => {
        this._ACCOUNTS_LOADING.next(false);
        this.init();
    });
  }

  public unUseCurrencyForProfile(currencyName: string): void {
    this._confirmDialogService.openConfirmDialog('Подтверждение', 'При отключении валюты все существующие операции в этой валюте будут удалены. Остатки на счетах в этой валюте будут обнулены. Продолжить?')
      .afterClosed()
      .pipe(
        filter((result: boolean) => result === true),
        tap(() => {
          this.loading = true;
          this._ACCOUNTS_LOADING.next(true);
        }),
        switchMap(() => this._profileService.updateProfileUnUseCurrency(currencyName)),
        tap(simpleResponse => {
          if (simpleResponse.status === 'FAIL') {
            this._alertService.addAlert(AlertType.WARNING, 'Во время сохранения произошла ошибка');
          } else {
            this._alertService.addAlert(AlertType.SUCCESS, 'Валюта успешно удалена');
          }
        }),
        switchMap(() => this._profileService.reloadCurrenciesAndAccountsInProfile())
      ).subscribe(() => {
        this._ACCOUNTS_LOADING.next(false);
        this.init();
      });
  }

  public markCurrencyAsDefault(currencyName: string): void {
    this.loading = true;
    this._profileService.markCurrencyAsDefault(currencyName)
      .pipe(
        tap(simpleResponse => {
          if (simpleResponse.status === 'FAIL') {
            this._alertService.addAlert(AlertType.WARNING, 'Во время сохранения произошла ошибка');
          } else {
            this._alertService.addAlert(AlertType.SUCCESS, 'Валюта успешно добавлена');
          }
        }),
        switchMap(() => this._profileService.reloadCurrenciesAndAccountsInProfile())
      ).subscribe(() => this.init());
  }

  public moveCurrencyDown(currencyName: string): void {
    this.loading = true;
    this._ACCOUNTS_LOADING.next(true);
    this.moveCurrency(this._profileService.moveCurrencyDown(currencyName));
  }

  public moveCurrencyUp(currencyName: string): void {
    this.loading = true;
    this._ACCOUNTS_LOADING.next(true);
    this.moveCurrency(this._profileService.moveCurrencyUp(currencyName));
  }

  public isMoveAvailable(): boolean {
    return this._profileService.getProfileCurrencies().length > 1;
  }

  public isNotLastUsed(currencyIndex: number): boolean {
    return this._profileService.getProfileCurrencies().length > currencyIndex + 1;
  }

  private moveCurrency(result: Observable<SimpleResponse>): void {
    result
      .pipe(
        tap(simpleResponse => {
          if (simpleResponse.status === 'FAIL') {
            this._alertService.addAlert(AlertType.WARNING, 'Во время сохранения произошла ошибка');
          } else {
            this._alertService.addAlert(AlertType.SUCCESS, 'Валюта успешно перемещена');
          }
        }),
        switchMap(() => this._profileService.reloadCurrenciesAndAccountsInProfile())
      ).subscribe(() => {
      this._ACCOUNTS_LOADING.next(false);
      this.init();
    });
  }

  private init(): void {
    this.loading = true;
    this._currencyService.loadAllCurrencies()
      .subscribe((currencies: CurrencyDetail[]) => {
        currencies.forEach((currency: CurrencyDetail) => {
          const currencyDetails: CurrencyDetail = this._profileService.getCurrencyDetails(currency.name);
          currency.order = currencyDetails ? currencyDetails.order : null;
          currency.defaultCurrency = currencyDetails ? currencyDetails.defaultCurrency : false;
        });
        this.allCurrencies = currencies;
        this.loading = false;
      });
  }
}
