import {Component, OnInit} from '@angular/core';

import {LoadingService} from '../../common/service/loading.service';
import {CurrencyService} from '../../common/service/currency.service';

@Component({
  selector: 'bk-summary',
  templateUrl: './summary.component.html',
  styleUrls: ['./summary.component.css']
})
export class SummaryComponent implements OnInit {
  public currencies: Currency[];

  public loading: boolean = true;

  public constructor(
    private _loadingService: LoadingService,
    private _currencyService: CurrencyService
  ) {}

  public ngOnInit(): void {
    this._currencyService.currencies.subscribe((currencies: Currency[]) => {
      this.currencies = currencies;
    });

    this._loadingService.summary$$.subscribe((value: boolean) => this.loading = value);

  }
}
