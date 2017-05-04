import {Component, OnInit} from '@angular/core';

import {LoadingService} from '../../common/service/loading.service';
import {SummaryService} from '../../common/service/summary.service';
import {AuthenticationService} from '../../common/service/authentication.service';

@Component({
  selector: 'bk-summary',
  templateUrl: './summary.component.html',
  styleUrls: ['./summary.component.css']
})
export class SummaryComponent implements OnInit {
  public loading: boolean = true;
  public conversionCurrency: Currency;
  public summaries: Summary[];

  public constructor(
    private _loadingService: LoadingService,
    private _summaryService: SummaryService,
    private _authenticationService: AuthenticationService
  ) {}

  public ngOnInit(): void {
    this._summaryService.loadSummaries(this._authenticationService.authenticatedProfile.id);
    this._loadingService.summary$$.subscribe((value: boolean) => this.loading = value);
    this._summaryService.summaries$.subscribe((summaries: Summary[]) => {
      this.summaries = summaries;
      this.loading = false;
    });
  }

  public setSummaryConversion(currency: Currency): void {
    this.conversionCurrency = currency;
  }
}
