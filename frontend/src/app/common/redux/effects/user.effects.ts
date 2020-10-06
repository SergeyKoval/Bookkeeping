import { Injectable } from '@angular/core';
import { Router } from '@angular/router';

import { Actions, createEffect, ofType } from '@ngrx/effects';
import { catchError, filter, map, switchMap, tap } from 'rxjs/operators';
import { forkJoin, of } from 'rxjs';
declare var $: any; // tslint:disable-line:no-any

import { UserActions } from '../actions';
import { AuthenticationService } from '../../service/authentication.service';
import { ProfileService } from '../../service/profile.service';
import { SubCategory } from '../reducers/user/category.reducer';
import { SubAccount } from '../reducers/user/account.reducer';
import { MatDialog } from '@angular/material/dialog';

@Injectable()
export class UserEffects {
  public checkAuthorization$ = createEffect(
    () => this._actions$.pipe(
      ofType(UserActions.CHECK_AUTHORIZATION),
      map(() => UserActions.LOAD_FULL_PROFILE({ redirectPage: [] }))
    )
  );

  public startAuthenticationExpirationJob$ = createEffect(
    () => this._actions$.pipe(
      ofType(UserActions.START_AUTHENTICATION_EXPIRATION_JOB),
      tap(() => this._authenticationService.startAuthenticationExpirationJob())
    ),
    { dispatch: false }
  );

  public loadFullProfile$ = createEffect(
    () => this._actions$.pipe(
      ofType(UserActions.LOAD_FULL_PROFILE),
      switchMap(({ redirectPage }) => forkJoin({
        redirectPage: of(redirectPage),
        profile: this._profileService.getUserProfile(),
      })),
      tap(({ redirectPage, profile }) => {
        profile.categories.forEach(category => category.subCategories.sort((first: SubCategory, second: SubCategory) => first.order - second.order));
        profile.accounts.forEach(account => account.subAccounts.sort((first: SubAccount, second: SubAccount) => first.order - second.order));
      }),
      switchMap(({ redirectPage, profile }) => [
        UserActions.LOAD_PROFILE_FINISHED({ profile }),
        UserActions.LOAD_CURRENCIES_FOR_CURRENT_MONTH({ redirectPage, profileCurrencies: profile.currencies.map((currency: CurrencyDetail) => currency.name) })
      ]),
      catchError(() => of(UserActions.LOAD_PROFILE_FAIL({ message: 'Ошибка при загрузке профиля пользователя' })))
    )
  );

  public changePage$ = createEffect(
    () => this._actions$.pipe(
      ofType(UserActions.CHANGE_PAGE),
      filter(({ redirectPage }) => redirectPage && redirectPage.length > 0),
      tap(({ redirectPage }) => this._router.navigate(redirectPage))
    ),
    { dispatch: false }
  );

  public closeDialogs$ = createEffect(
    () => this._actions$.pipe(
      ofType(UserActions.CLOSE_DIALOGS),
      tap(() => this._dialogRef.closeAll())
    ),
    { dispatch: false }
  );

  public showAlert$ = createEffect(
    () => this._actions$.pipe(
      ofType(UserActions.SHOW_ALERT),
      tap(({ alert } ) => {
        $.notify({
          'title': alert.title,
          'message': alert.message
        }, {
          'z_index': 999,
          'offset': {'x': UserEffects.calculateHorizontalOffset(), 'y': 55},
          'type': alert.type
        });
      })
    ),
    { dispatch: false }
  );

  public constructor (
    private _actions$: Actions,
    private _router: Router,
    private _authenticationService: AuthenticationService,
    private _profileService: ProfileService,
    private _dialogRef: MatDialog,
  ) {}

  private static calculateHorizontalOffset(): number {
    const viewWidth: number = window.innerWidth;
    if (viewWidth > 1200) {
      return (window.innerWidth - 1170) / 2 + 15;
    } else if (viewWidth > 992) {
      return (window.innerWidth - 970) / 2 + 15;
    } else {
      return (window.innerWidth - 750) / 2 + 15;
    }
  }
}
