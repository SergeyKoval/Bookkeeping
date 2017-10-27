import { Component, OnInit } from '@angular/core';

import { LoadingService } from '../../common/service/loading.service';
import { SettingsService } from '../../common/service/settings.service';

@Component({
  selector: 'bk-summary',
  templateUrl: './summary.component.html',
  styleUrls: ['./summary.component.css']
})
export class SummaryComponent implements OnInit {
  public loading: boolean = true;
  public conversionCurrency: CurrencyDetail;
  public accounts: FinAccount[];

  public constructor(
    private _settingService: SettingsService,
    private _loadingService: LoadingService
  ) {}

  public ngOnInit(): void {
    this._loadingService.accounts$$.subscribe((value: boolean) => this.loading = value);
    this._settingService.accounts$.subscribe((accounts: FinAccount[]) => {
      this.accounts = accounts;
    });
  }

  public setSummaryConversion(currency: CurrencyDetail): void {
    this.conversionCurrency = currency;
  }
}
