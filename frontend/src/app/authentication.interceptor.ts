import { Injectable } from '@angular/core';
import { HttpEvent, HttpHandler, HttpInterceptor, HttpRequest } from '@angular/common/http';

import { Observable, of } from 'rxjs/index';
import { catchError} from 'rxjs/internal/operators';
import { AuthenticationService } from './common/service/authentication.service';

@Injectable()
export class AuthenticationInterceptor implements HttpInterceptor {

  public constructor(private _authenticationService: AuthenticationService) {}

  public intercept(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    return next.handle(req).pipe(catchError(err => {
      if (err.url.endsWith('/token/generate-token') && err.status === 401) {
        if (err.error === 'BAD CREDENTIALS') {
          this._authenticationService.addErrorMessage('Неверный пароль');
        }
      } else {
        console.log(err);
        this._authenticationService.exit();
      }

      return of(err);
    }));
  }
}