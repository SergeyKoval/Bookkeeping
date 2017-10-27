import { Component, EventEmitter, OnInit, Output } from '@angular/core';

import { AuthenticationService } from '../../../common/service/authentication.service';
import { CurrencyService } from '../../../common/service/currency.service';
import { AlertService } from '../../../common/service/alert.service';
import { AlertType } from '../../../common/model/alert/AlertType';

@Component({
  selector: 'bk-currency-conversion',
  templateUrl: './currency-conversion.component.html',
  styleUrls: ['./currency-conversion.component.css']
})
export class CurrencyConversionComponent implements OnInit {
  @Output()
  public currencyConversion: EventEmitter<CurrencyDetail> = new EventEmitter();

  public currencies: CurrencyDetail[];
  public selectedCurrency: CurrencyDetail = null;

  public constructor(
    private _authenticationService: AuthenticationService,
    private _currencyService: CurrencyService,
    private _alertService: AlertService
  ) { }

  public ngOnInit(): void {
    this.currencies = this._authenticationService.authenticatedProfile.currencies;
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
