import { Injectable } from '@angular/core';
import { ActivatedRouteSnapshot, CanActivate, RouterStateSnapshot } from '@angular/router';

import { Observable, of } from 'rxjs';
import { exhaustMap, filter } from 'rxjs/operators';
import { select, Store } from '@ngrx/store';

import * as fromUser from '../redux/reducers/user';
import { AuthenticationService } from '../service/authentication.service';
import { UserActions } from '../redux/actions';

@Injectable({
  providedIn: 'root'
})
export class AuthenticationGuard implements CanActivate {
  public constructor(
    private _userStore: Store<fromUser.State>,
    private _authenticationService: AuthenticationService
  ) {}

  public canActivate(route: ActivatedRouteSnapshot, state: RouterStateSnapshot): Observable<boolean> | Promise<boolean> | boolean {
    if (!this._authenticationService.validateToken()) {
      return false;
    }

    return this._userStore.pipe(
      select(fromUser.selectInitialDataReady),
      exhaustMap(initialDataReady => {
        if (!!initialDataReady) {
          return of(initialDataReady);
        }

        this._userStore.dispatch(UserActions.CHECK_AUTHORIZATION());
        return this._userStore.select(fromUser.selectInitialDataReady).pipe(filter(ready => !!ready));
      })
    );
  }
}
