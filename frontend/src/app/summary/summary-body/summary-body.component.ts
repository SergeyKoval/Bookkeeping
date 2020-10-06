import { ChangeDetectionStrategy, Component, Input } from '@angular/core';

import { Store } from '@ngrx/store';

import { Account } from '../../common/redux/reducers/user/account.reducer';
import * as fromUser from '../../common/redux/reducers/user';
import { SummaryActions } from '../../common/redux/actions';

@Component({
  selector: 'bk-summary-body',
  templateUrl: './summary-body.component.html',
  styleUrls: ['./summary-body.component.css'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class SummaryBodyComponent {
  @Input()
  public accounts: Array<Account>;
  @Input()
  public conversionCurrency: CurrencyDetail;

  public constructor(private _userStore: Store<fromUser.State>) {}

  public changeOpenState(account: Account): void {
    this._userStore.dispatch(SummaryActions.TOGGLE_ACCOUNT({ account: account.title, opened: !account.opened }));
  }
}
