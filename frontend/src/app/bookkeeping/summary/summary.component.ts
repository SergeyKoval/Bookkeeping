import {Component, Input, OnInit} from '@angular/core';

import {LoadingService} from '../../common/service/loading.service';
import {CurrencyService} from '../../common/service/currency.service';
import {AuthenticationService} from '../../common/service/authentication.service';

@Component({
  selector: 'bk-summary',
  templateUrl: './summary.component.html',
  styleUrls: ['./summary.component.css']
})
export class SummaryComponent implements OnInit {
  @Input()
  public currencies: Currency[];

  public loading: boolean = true;

  public constructor(
    private _loadingService: LoadingService,
    private _currencyService: CurrencyService,
    private _authenticationService: AuthenticationService
  ) {}

  public ngOnInit(): void {
    this._loadingService.summary$$.subscribe((value: boolean) => this.loading = value);

  }
}
