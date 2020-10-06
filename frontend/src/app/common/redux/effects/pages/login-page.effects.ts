import { Injectable } from '@angular/core';
import { Router } from '@angular/router';

import { Actions, createEffect, ofType } from '@ngrx/effects';
import { forkJoin, of } from 'rxjs';
import { catchError, exhaustMap, map, switchMap, tap } from 'rxjs/operators';

import { AuthenticationService } from '../../../service/authentication.service';
import { ProfileService } from '../../../service/profile.service';
import { ApplicationActions, LoginPageActions, UserActions } from '../../actions';
import { AlertType } from '../../reducers';

@Injectable()
export class LoginPageEffects {
  public loginRedirect$ = createEffect(
    () => this._actions$.pipe(
      ofType(LoginPageActions.LOGIN_REDIRECT),
      tap( () => this._router.navigate(['/authentication']))
    ),
    { dispatch: false }
  );

  public checkServerVersion$ = createEffect(
    () => this._actions$.pipe(
      ofType(ApplicationActions.CHECK_SERVER_VERSION),
      switchMap(({ frontendVersion }) => forkJoin({
        frontendVersion: of(frontendVersion),
        backendVersionResponse: this._authenticationService.getServerVersion()
      })),
      map(({ frontendVersion, backendVersionResponse }) => ApplicationActions.CHECK_SERVER_VERSION_FINISHED({
        backendVersion: backendVersionResponse.message,
        versionError: !backendVersionResponse.message.startsWith(frontendVersion)
      })),
      catchError(() => of(UserActions.SHOW_ALERT({ alert: { type: AlertType.WARNING, message: 'Ошибка при получении версии приложения' } })))
    )
  );

  public sendCode$ = createEffect(
    () => this._actions$.pipe(
      ofType(LoginPageActions.SEND_CODE),
      exhaustMap(({ registrationData }) =>
        this._authenticationService.sendRegistrationCode(registrationData).pipe(
          map(response => {
            if (response.status === 'FAIL') {
              return LoginPageActions.SEND_CODE_FINISHED({ message: response.message === 'ALREADY_EXIST' ? 'Пользователь уже зарегистрирован' : 'Ошибка при отправке кода подтверждения' });
            } else {
              return LoginPageActions.SEND_CODE_FINISHED({ message: null });
            }
          }),
          catchError(() => of(LoginPageActions.SEND_CODE_FINISHED({ message: 'Ошибка при отправке кода подтверждения' })))
        )
      )
    )
  );

  public reviewCode$ = createEffect(
    () => this._actions$.pipe(
      ofType(LoginPageActions.REVIEW_CODE),
      exhaustMap(({ registrationData }) =>
        this._authenticationService.reviewRegistrationCode(registrationData).pipe(
          map(response => {
            if (response.status === 'FAIL') {
              return LoginPageActions.REVIEW_CODE_FINISHED({ message: response.message === 'INVALID_CODE' ? 'Код не совпадает' : 'Ошибка при проверке кода' });
            } else {
              return UserActions.LOAD_FULL_PROFILE({ redirectPage: ['settings', 'profile'] });
            }
          }),
          catchError(() => of(LoginPageActions.REVIEW_CODE_FINISHED({ message: 'Ошибка при отправке кода подтверждения' })))
        )
      )
    )
  );

  public authenticate$ = createEffect(
    () => this._actions$.pipe(
      ofType(LoginPageActions.AUTHENTICATE),
      exhaustMap(({ credentials }) =>
        this._authenticationService.authenticate(credentials).pipe(
          map(() => UserActions.LOAD_FULL_PROFILE({ redirectPage: ['budget'] })),
          catchError(() => of(LoginPageActions.AUTHENTICATION_FINISHED({ message: 'Ошибка при попытке аутентификации' })))
        )
      )
    )
  );

  public authenticationFailed$ = createEffect(
    () => this._actions$.pipe(
      ofType(LoginPageActions.AUTHENTICATION_FAILED),
      map(({ messageCode }) => {
        switch (messageCode) {
          case 'MISSED USER':
            return LoginPageActions.NOTIFY_MISSED_USER({ message: 'Несуществующий пользователь' });
          case 'BAD CREDENTIALS':
            return LoginPageActions.AUTHENTICATION_FINISHED({ message: 'Неверный пароль' });
          case 'NOT ACTIVE':
            return LoginPageActions.SHOW_REGISTRATION_FORM({ restorePasswordForm: false, message: 'Пользователь не активирован', alwaysEditing: true });
        }
      })
    )
  );

  public constructor (
    private _actions$: Actions,
    private _router: Router,
    private _authenticationService: AuthenticationService,
    private _profileService: ProfileService
  ) {}
}
