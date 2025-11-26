import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { Router } from '@angular/router';

import { ProfileService } from '../../service/profile.service';
import { CurrencyService } from '../../service/currency.service';
import { AlertService } from '../../service/alert.service';
import { AlertType } from '../../model/alert/AlertType';
import { CurrencyDetail } from '../../model/currency-detail';
import { Profile } from '../../model/profile';

@Component({
    selector: 'bk-currency-conversion',
    templateUrl: './currency-conversion.component.html',
    styleUrls: ['./currency-conversion.component.css'],
    standalone: false
})
export class CurrencyConversionComponent implements OnInit {
  @Input()
  public label: string = 'к валюте:';
  @Input()
  public selectedCurrency: CurrencyDetail = null;
  @Input()
  public denyNoChoice: boolean = false;
  @Output()
  public currencyConversion: EventEmitter<CurrencyDetail> = new EventEmitter();

  public profile: Profile;

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
