import { Injectable } from '@angular/core';
import { HttpEvent, HttpHandler, HttpInterceptor, HttpRequest, HttpResponse } from '@angular/common/http';

import { Observable, of } from 'rxjs';
import { catchError, tap } from 'rxjs/internal/operators';
import { Store } from '@ngrx/store';

import { AuthenticationService } from './common/service/authentication.service';
import { environment } from '../environments/environment';
import { AlertType, fromLoginPage } from './common/redux/reducers';
import { LoginPageActions, UserActions } from './common/redux/actions';

@Injectable()
export class AuthenticationInterceptor implements HttpInterceptor {

  public constructor(
    private _authenticationService: AuthenticationService,
    private _store: Store<fromLoginPage.LoginPageState>
  ) {}

  // tslint:disable-next-line:no-any
  public intercept(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    if ((!req.url.endsWith('/token/generate-token') && !req.url.endsWith('/token/send-registration-code')
      && !req.url.endsWith('/token/review-registration-code') && !req.url.match(/token\/server\/version/))
      && !this._authenticationService.validateToken()
    ) {
      return of();
    }

    if (!req.url.endsWith('/token/refresh-token')) {
      this._authenticationService.refreshTokenIfRequired();
    }

    return next.handle(req).pipe(
      // tslint:disable-next-line:no-any
      tap((response: HttpResponse<any>) => {
        if (response.url && !req.url.match(/token\/server\/version/) && !response.headers.get('bk-version').startsWith(environment.VERSION)) {
          console.error(`Versions mismatch. Server version = ${response.headers.get('bk-version')}, ui version = ${environment.VERSION}`);
          this._store.dispatch(UserActions.SHOW_ALERT({ alert: { type: AlertType.DANGER, message: 'Версия устарела. Обновите страницу.' } }));
          this._authenticationService.exit(false);
        }
      }),
      catchError(err => {
        if ((err.url.endsWith('/token/generate-token') || err.url.endsWith('/token/send-registration-code')) && (err.status === 401 || err.status === 403)) {
          if (err.status === 401) {
            this._store.dispatch(LoginPageActions.AUTHENTICATION_FAILED({ messageCode: err.error }));
          } else if (err.status === 403) {
            this._store.dispatch(LoginPageActions.AUTHENTICATION_FAILED({ messageCode: 'NOT ACTIVE' }));
          }
        } else {
          console.log(err);
          if (err.status === 401) {
            this._authenticationService.exit(true);
          } else {
            this._store.dispatch(UserActions.CLOSE_DIALOGS());
            this._store.dispatch(UserActions.SHOW_ALERT({ alert: { type: AlertType.DANGER, message: 'Возникла ошибка при обработке запроса, обратитесь к администратору' } }));
          }
        }

        return of(err);
      }));
  }
}
