import { Injectable } from '@angular/core';
import { ActivatedRouteSnapshot, CanActivate, RouterStateSnapshot } from '@angular/router';

import { Observable, of } from 'rxjs';
import { select, Store } from '@ngrx/store';
import { catchError, map } from 'rxjs/operators';

import * as fromUser from '../redux/reducers/user';

@Injectable({
  providedIn: 'root'
})
export class AdminGuard implements CanActivate {
  public constructor(private _userStore: Store<fromUser.State>) {}

  public canActivate(next: ActivatedRouteSnapshot, state: RouterStateSnapshot): Observable<boolean> {
    return this._userStore.pipe(select(fromUser.selectProfile)).pipe(
      map((profile) => profile.roles.indexOf('ADMIN') > -1),
      catchError(() => of(false))
    );
  }
}
