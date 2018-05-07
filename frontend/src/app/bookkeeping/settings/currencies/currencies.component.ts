import { Component, OnInit } from '@angular/core';
import { NavigationEnd, Router } from '@angular/router';
import { RouterEvent } from '@angular/router/src/events';

import { Subscription } from 'rxjs/Subscription';

import { CurrencyService } from '../../../common/service/currency.service';
import { ProfileService } from '../../../common/service/profile.service';
import { filter, switchMap, tap } from 'rxjs/operators';
import { ConfirmDialogService } from '../../../common/components/confirm-dialog/confirm-dialog.service';

@Component({
  selector: 'bk-currencies',
  templateUrl: './currencies.component.html',
  styleUrls: ['./currencies.component.css']
})
export class CurrenciesComponent implements OnInit {
  public loading: boolean = true;
  public allCurrencies: CurrencyDetail[];

  private _navigationSubscription: Subscription;

  public constructor(
    private _router: Router,
    private _confirmDialogService: ConfirmDialogService,
    private _currencyService: CurrencyService,
    private _profileService: ProfileService
  ) {
    this._navigationSubscription = this._router.events.subscribe((e: RouterEvent) => {
      if (e instanceof NavigationEnd && e.urlAfterRedirects.endsWith('reload=true')) {
        this.init();
      }
    });
  }

  public ngOnInit(): void {
    this.init();
  }

  public isCurrencyUsed(currencyName: string): boolean {
    return this._profileService.isCurrencyUsed(currencyName);
  }

  public isCurrencyDefault(currencyName: string): boolean {
    const currencyDetails: CurrencyDetail = this._profileService.getCurrencyDetails(currencyName);
    return currencyDetails ? currencyDetails.default : false;
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
    this._currencyService.loadAllCurrencies()
      .subscribe((currencies: CurrencyDetail[]) => {
        currencies.forEach((currency: CurrencyDetail) => {
          const currencyDetails: CurrencyDetail = this._profileService.getCurrencyDetails(currency.name);
          currency.order = currencyDetails ? currencyDetails.order : null;
        });
        this.allCurrencies = currencies;
        this.loading = false;
      });
  }
}
