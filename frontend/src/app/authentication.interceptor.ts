import { Injectable } from '@angular/core';
import { HttpEvent, HttpHandler, HttpInterceptor, HttpRequest } from '@angular/common/http';

import { Observable, of } from 'rxjs/index';
import { catchError} from 'rxjs/internal/operators';

import { AuthenticationService } from './common/service/authentication.service';
import { DialogService } from './common/service/dialog.service';

@Injectable()
export class AuthenticationInterceptor implements HttpInterceptor {

  public constructor(
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
      } else {
        console.log(err);
        this._dialogService.closeAllDialogs();
        this._authenticationService.exit(true);
      }

      return of(err);
    }));
  }
}
