import { Component, EventEmitter, OnInit, Output } from '@angular/core';
import { Router } from '@angular/router';

import { ProfileService } from '../../service/profile.service';
import { CurrencyService } from '../../service/currency.service';
import { AlertService } from '../../service/alert.service';
import { AlertType } from '../../model/alert/AlertType';

@Component({
  selector: 'bk-currency-conversion',
  templateUrl: './currency-conversion.component.html',
  styleUrls: ['./currency-conversion.component.css']
})
export class CurrencyConversionComponent implements OnInit {
  @Output()
  public currencyConversion: EventEmitter<CurrencyDetail> = new EventEmitter();

  public profile: Profile;
  public selectedCurrency: CurrencyDetail = null;

  public constructor(
    private _router: Router,
    private _authenticationService: ProfileService,
    private _currencyService: CurrencyService,
    private _alertService: AlertService
  ) {}

  public ngOnInit(): void {
    this.profile = this._authenticationService.authenticatedProfile;
  }

  public chooseCurrency(currency: CurrencyDetail): void {
    if (currency === null || this._currencyService.isCurrencyHistoryLoaded(currency.name)) {
      this.selectedCurrency = currency;
      this.currencyConversion.emit(currency);
    } else {
      this._alertService.addAlert(AlertType.WARNING, 'Альтернативные валюты еще не загружены. Попробуйте немного позже.');
    }
  }
}
