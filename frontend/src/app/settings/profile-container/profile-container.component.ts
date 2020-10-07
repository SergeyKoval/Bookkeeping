import { ChangeDetectionStrategy, Component, OnInit } from '@angular/core';

import { select, Store } from '@ngrx/store';
import { Observable } from 'rxjs';

import * as fromUser from '../../common/redux/reducers/user';
import { fromSettingsPage } from '../../common/redux/reducers';

@Component({
  selector: 'bk-profile',
  template: `<bk-profile-form [profileEmail]="profileEmail$ | async" [submitIndicator]="submitIndicator$ | async"></bk-profile-form>`,
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class ProfileContainerComponent implements OnInit {
  public profileEmail$: Observable<string>;
  public submitIndicator$: Observable<boolean>;

  public constructor(
    private _userStore: Store<fromUser.State>,
    private _store: Store<fromSettingsPage.State>
  ) {}

  public ngOnInit(): void {
    this.profileEmail$ = this._userStore.pipe(select(fromUser.selectProfileEmail));
    this.submitIndicator$ = this._store.pipe(select(fromSettingsPage.selectProfileSubmitIndicator));
  }
}
