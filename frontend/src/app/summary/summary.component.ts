import { ChangeDetectionStrategy, Component, OnInit } from '@angular/core';

import { select, Store } from '@ngrx/store';
import { Observable } from 'rxjs';

import { LoadingService } from '../common/service/loading.service';
import { ProfileService } from '../common/service/profile.service';
import * as fromUser from '../common/redux/reducers/user';
import { Account } from '../common/redux/reducers/user/account.reducer';

@Component({
  selector: 'bk-summary',
  templateUrl: './summary.component.html',
  styleUrls: ['./summary.component.css'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class SummaryComponent implements OnInit {
  public accountsLoading$: Observable<boolean>;
  public accounts$: Observable<Array<Account>>;
  public conversionCurrency: CurrencyDetail;

  public constructor(
    private _userStore: Store<fromUser.State>,
    private _authenticationService: ProfileService,
    private _loadingService: LoadingService
  ) {}

  public ngOnInit(): void {
    this.accountsLoading$ = this._userStore.pipe(select(fromUser.selectAccountsLoading));
    this.accounts$ = this._userStore.pipe(select(fromUser.selectAccounts));
  }

  public setSummaryConversion(currency: CurrencyDetail): void {
    this.conversionCurrency = currency;
  }
}
