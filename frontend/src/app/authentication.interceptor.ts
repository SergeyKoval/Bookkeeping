import { Injectable } from '@angular/core';
import { HttpEvent, HttpHandler, HttpInterceptor, HttpRequest, HttpResponse } from '@angular/common/http';

import { Observable, of } from 'rxjs';
import { catchError, tap } from 'rxjs/internal/operators';

import { AuthenticationService } from './common/service/authentication.service';
import { DialogService } from './common/service/dialog.service';
import { AlertService } from './common/service/alert.service';
import { AlertType } from './common/model/alert/AlertType';
import { environment } from '../environments/environment';

@Injectable()
export class AuthenticationInterceptor implements HttpInterceptor {

  public constructor(
    private _alertService: AlertService,
    private _authenticationService: AuthenticationService,
    private _dialogService: DialogService
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
        if (response.url && !req.url.match(/token\/server\/version/) && response.headers.get('bk-version') !== environment.VERSION) {
          console.error(`Versions mismatch. Server version = ${response.headers.get('bk-version')}, ui version = ${environment.VERSION}`);
          this._dialogService.closeAllDialogs();
          this._alertService.addAlert(AlertType.DANGER, 'Версия устарела. Обновите страницу.');
          this._authenticationService.exit(false);
        }
      }),
      catchError(err => {
        if ((err.url.endsWith('/token/generate-token') || err.url.endsWith('/token/send-registration-code')) && (err.status === 401 || err.status === 403)) {
          if (err.status === 401) {
            this._authenticationService.addErrorMessage(err.error);
          } else if (err.status === 403) {
            this._authenticationService.addErrorMessage('NOT ACTIVE');
          }
        } else if (err.status === 401) {
          console.log(err);
          this._dialogService.closeAllDialogs();
          this._authenticationService.exit(true);
        } else {
          console.log(err);
          this._dialogService.closeAllDialogs();
          this._alertService.addAlert(AlertType.DANGER, 'Возникла ошибка при обработке запроса, обратитесь к администратору');
        }

        return of(err);
      }));
  }
}
