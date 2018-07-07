import { Injectable } from '@angular/core';
import { HttpEvent, HttpHandler, HttpInterceptor, HttpRequest } from '@angular/common/http';

import { Observable, of } from 'rxjs/index';
import { catchError } from 'rxjs/internal/operators';

import { AuthenticationService } from './common/service/authentication.service';
import { DialogService } from './common/service/dialog.service';
import { AlertService } from './common/service/alert.service';
import { AlertType } from './common/model/alert/AlertType';

@Injectable()
export class AuthenticationInterceptor implements HttpInterceptor {

  public constructor(
    private _alertService: AlertService,
    private _authenticationService: AuthenticationService,
    private _dialogService: DialogService
  ) {}

  // tslint:disable-next-line:no-any
  public intercept(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    if (!req.url.endsWith('/token/generate-token') && !this._authenticationService.validateToken()) {
      return of();
    }

    if (!req.url.endsWith('/token/refresh-token')) {
      this._authenticationService.refreshTokenIfRequired();
    }

    return next.handle(req).pipe(catchError(err => {
      if (err.url.endsWith('/token/generate-token') && err.status === 401) {
        if (err.error === 'BAD CREDENTIALS') {
          this._authenticationService.addErrorMessage('Неверный пароль');
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
