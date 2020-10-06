import { ChangeDetectionStrategy, Component, OnInit } from '@angular/core';

import { Observable } from 'rxjs';
import { select, Store } from '@ngrx/store';

import { BrowserUtils } from '../../common/utils/browser-utils';
import { fromLoginPage } from '../../common/redux/reducers';
import * as fromUser from '../../common/redux/reducers/user';

@Component({
  selector: 'bk-authentication-container',
  templateUrl: './authentication-container.component.html',
  styleUrls: ['./authentication-container.component.css'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class AuthenticationContainerComponent implements OnInit {
  public initialDataLoading$: Observable<boolean>;
  public authenticationMode$: Observable<boolean>;
  public authenticationChecking$: Observable<boolean>;
  public formSubmitted$: Observable<boolean>;
  public errorMessage$: Observable<string>;
  public alwaysEditing$: Observable<boolean>;

  public unsupportedBrowser: boolean;

  public constructor(
    private _store: Store<fromLoginPage.LoginPageState>,
    private _userStore: Store<fromUser.State>,
  ) {}

  public ngOnInit(): void {
    this.unsupportedBrowser = !BrowserUtils.isSupportedBrowser();
    this.authenticationMode$ = this._store.pipe(select(fromLoginPage.selectMode));
    this.errorMessage$ = this._store.pipe(select(fromLoginPage.selectError));
    this.initialDataLoading$ = this._userStore.pipe(select(fromUser.selectInitialDataLoading));
    this.authenticationChecking$ = this._store.pipe(select(fromLoginPage.selectAuthenticationChecking));
    this.formSubmitted$ = this._store.pipe(select(fromLoginPage.selectFormSubmitted));
    this.alwaysEditing$ = this._store.pipe(select(fromLoginPage.selectAlwaysEditing));
  }

  public getBrowserWidth(): number {
    return window.innerWidth;
  }

  public isResolutionUnsupported(): boolean {
    return window.innerWidth < 768;
  }
}
