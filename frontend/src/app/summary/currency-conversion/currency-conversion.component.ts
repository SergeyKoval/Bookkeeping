import { Component, EventEmitter, OnDestroy, OnInit, Output } from '@angular/core';
import { NavigationEnd, Router } from '@angular/router';
import { RouterEvent } from '@angular/router/src/events';

import { Subscription } from 'rxjs';

import { ProfileService } from '../../common/service/profile.service';
import { CurrencyService } from '../../common/service/currency.service';
import { AlertService } from '../../common/service/alert.service';
import { AlertType } from '../../common/model/alert/AlertType';

@Component({
  selector: 'bk-currency-conversion',
  templateUrl: './currency-conversion.component.html',
  styleUrls: ['./currency-conversion.component.css']
})
export class CurrencyConversionComponent implements OnInit, OnDestroy {
  @Output()
  public currencyConversion: EventEmitter<CurrencyDetail> = new EventEmitter();

  public currencies: CurrencyDetail[];
  public selectedCurrency: CurrencyDetail = null;

  private _reloadNavigation: Subscription;

  public constructor(
    private _router: Router,
    private _authenticationService: ProfileService,
    private _currencyService: CurrencyService,
    private _alertService: AlertService
  ) {
    this._reloadNavigation = this._router.events.subscribe((e: RouterEvent) => {
      if (e instanceof NavigationEnd && e.urlAfterRedirects.endsWith('reload=true')) {
        this.currencies = this._authenticationService.authenticatedProfile.currencies;
      }
    });
  }

  public ngOnInit(): void {
    this.currencies = this._authenticationService.authenticatedProfile.currencies;
  }

  public ngOnDestroy(): void {
    this._reloadNavigation.unsubscribe();
  }

  public chooseCurrency(currency: CurrencyDetail): void {
    if (currency === null || this._currencyService.isCurrencyHistoryLoaded(currency.name)) {
      this.selectedCurrency = currency;
      this.currencyConversion.emit(currency);
    } else {
      this._alertService.addAlert(AlertType.WARNING, 'Альтернативные валюты еще не загружены. Попробуйте немного позже.', 'Ошибка', 3);
    }
  }
}
