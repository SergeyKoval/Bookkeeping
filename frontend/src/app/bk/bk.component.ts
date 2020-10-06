import { ChangeDetectionStrategy, Component, OnInit } from '@angular/core';
import { Title } from '@angular/platform-browser';
import { ActivatedRoute, NavigationEnd, Router } from '@angular/router';

import { Observable } from 'rxjs';
import { filter, map } from 'rxjs/internal/operators';
import { select, Store } from '@ngrx/store';

import { environment } from '../../environments/environment';
import * as fromUser from '../common/redux/reducers/user';
import { fromLoginPage } from '../common/redux/reducers';
import { ApplicationActions } from '../common/redux/actions';

@Component({
  selector: 'bk-root',
  templateUrl: './bk.component.html',
  styleUrls: ['./bk.component.css'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class BookkeepingRootComponent implements OnInit {
  public uiVersion: string = environment.VERSION;

  public initialDataReady$: Observable<boolean>;
  public versionError$: Observable<boolean>;
  public permissionsChecking$: Observable<boolean>;
  public versionChecking$: Observable<boolean>;
  public serverVersion$: Observable<string>;

  public constructor(
    private _router: Router,
    private _activatedRoute: ActivatedRoute,
    private _titleService: Title,
    private _userStore: Store<fromUser.State>,
    private _store: Store<fromLoginPage.LoginPageState>
  ) {}

  public ngOnInit(): void {
    this.initialDataReady$ = this._userStore.pipe(select(fromUser.selectInitialDataReady));
    this.permissionsChecking$ = this._userStore.pipe(select(fromUser.selectPermissionsChecking));
    this.versionError$ = this._store.pipe(select(fromLoginPage.selectVersionError));
    this.versionChecking$ = this._store.pipe(select(fromLoginPage.selectVersionChecking));
    this.serverVersion$ = this._store.pipe(select(fromLoginPage.selectServerVersion));

    this._store.dispatch(ApplicationActions.CHECK_SERVER_VERSION({ frontendVersion: this.uiVersion }));

    this._router.events
      .pipe(
        filter(event => event instanceof NavigationEnd),
        map(value => {
          let child: ActivatedRoute = this._activatedRoute.firstChild;
          while (child) {
            if (child.firstChild) {
              child = child.firstChild;
            } else if (child.snapshot.data && child.snapshot.data['title']) {
              return child.snapshot.data['title'];
            } else {
              return null;
            }
          }
          return null;
        })
      ).subscribe((title: string) => this._titleService.setTitle(title));
  }
}
