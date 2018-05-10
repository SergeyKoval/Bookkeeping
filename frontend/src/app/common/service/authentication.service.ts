import { Injectable } from '@angular/core';
import { ActivatedRouteSnapshot, CanActivate, Router, RouterStateSnapshot } from '@angular/router';

import { LocalStorageService } from 'angular-2-local-storage';
import { Observable } from 'rxjs/index';
import { JwtHelperService } from '@auth0/angular-jwt';
import { isNullOrUndefined } from "util";

@Injectable({
  providedIn: 'root'
})
export class AuthenticationService implements CanActivate {
  public static readonly TOKEN: string = 'access_token';

  public constructor(
    private _router: Router,
    private _localStorageService: LocalStorageService,
    private _jwtHelper: JwtHelperService
  ) {}

  public canActivate(route: ActivatedRouteSnapshot, state: RouterStateSnapshot): Observable<boolean> | Promise<boolean> | boolean {
    const token: string = this._localStorageService.get(AuthenticationService.TOKEN);
    if (!isNullOrUndefined(token) && !this._jwtHelper.isTokenExpired(token)) {
      return true;
    }

    this._router.navigate(['/authentication']);
    return false;
  }
}
