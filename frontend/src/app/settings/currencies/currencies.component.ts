import { Component, OnDestroy, OnInit } from '@angular/core';
import { NavigationEnd, Router, RouterEvent } from '@angular/router';

import { Subscription } from 'rxjs';
import { filter, switchMap, tap } from 'rxjs/operators';

import { CurrencyService } from '../../common/service/currency.service';
import { ProfileService } from '../../common/service/profile.service';
import { ConfirmDialogService } from '../../common/components/confirm-dialog/confirm-dialog.service';

@Component({
  selector: 'bk-currencies',
  templateUrl: './currencies.component.html',
  styleUrls: ['./currencies.component.css']
})
export class CurrenciesComponent implements OnInit, OnDestroy {
  public loading: boolean = true;
  public allCurrencies: CurrencyDetail[];

  private _NAVIGATION_SUBSCRIPTION: Subscription;

  public constructor(
    private _router: Router,
    private _confirmDialogService: ConfirmDialogService,
    private _currencyService: CurrencyService,
    private _profileService: ProfileService
  ) {
    this._NAVIGATION_SUBSCRIPTION = this._router.events.subscribe((e: RouterEvent) => {
      if (e instanceof NavigationEnd && e.urlAfterRedirects.endsWith('reload=true')) {
        this.init();
      }
    });
  }

  public ngOnInit(): void {
    this.init();
  }

  public ngOnDestroy(): void {
    this._NAVIGATION_SUBSCRIPTION.unsubscribe();
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
    this._profileService.reloadProfile().subscribe(value => {
      this._router.navigate(['settings', 'currencies'], {queryParams: {reload: true}});
    });
  }

  public unUseCurrencyForProfile(currencyName: string): void {
    const subscription: Subscription = this._confirmDialogService.openConfirmDialog('Подтверждение', 'При отключении валюты все существующие операции в этой валюте будут удалены. Продолжить?')
      .afterClosed()
      .pipe(
        filter((result: boolean) => result === true),
        tap((result: boolean) => this.loading = true),
        switchMap(() => this._profileService.reloadProfile())
      ).subscribe((profiles: Profile[]) => {
        this._router.navigate(['settings', 'currencies'], {queryParams: {reload: true}});
        subscription.unsubscribe();
      });
  }

  public markCurrencyAsDefault(currency: string): void {

  }

  public moveCurrencyDown(currency: string): void {

  }

  public moveCurrencyUp(currency: string): void {

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
