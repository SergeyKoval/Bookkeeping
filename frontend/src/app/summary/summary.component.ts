import { Component, OnInit } from '@angular/core';

import { LoadingService } from '../common/service/loading.service';
import { ProfileService } from '../common/service/profile.service';
import { CurrencyService } from '../common/service/currency.service';
import { CurrencyDetail } from '../common/model/currency-detail';
import { FinAccount } from '../common/model/fin-account';

@Component({
    selector: 'bk-summary',
    templateUrl: './summary.component.html',
    styleUrls: ['./summary.component.css'],
    standalone: false
})
export class SummaryComponent implements OnInit {
  public loading: boolean = false;
  public conversionCurrency: CurrencyDetail;
  public accounts: FinAccount[];

  public constructor(
    private _authenticationService: ProfileService,
    private _loadingService: LoadingService,
    private _currencyService: CurrencyService
  ) {}

  public ngOnInit(): void {
    this._loadingService.accounts$$.subscribe((value: boolean) => this.loading = value);
    this._authenticationService.accounts$.subscribe((accounts: FinAccount[]) => this.accounts = accounts);
  }

  public setSummaryConversion(currency: CurrencyDetail): void {
    this.conversionCurrency = currency;
    this._currencyService.setConversionCurrency(currency);
  }
}
