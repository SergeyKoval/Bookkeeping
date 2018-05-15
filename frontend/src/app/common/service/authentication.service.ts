import { Injectable } from '@angular/core';
import { ActivatedRouteSnapshot, CanActivate, Router, RouterStateSnapshot } from '@angular/router';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { HttpClient, HttpResponse } from '@angular/common/http';

import { LocalStorageService } from 'angular-2-local-storage';
import { Observable, Subject } from 'rxjs/index';
import { JwtHelperService } from '@auth0/angular-jwt';
import { isNullOrUndefined } from "util";
import { tap } from 'rxjs/internal/operators';

import { LoadingService } from './loading.service';
import { ProfileService } from './profile.service';

@Injectable({
  providedIn: 'root'
})
export class AuthenticationService implements CanActivate {
  public static readonly TOKEN: string = 'access_token';

  public constructor(
    private _profileService: ProfileService,
    private _loadingService: LoadingService,
    private _formBuilder: FormBuilder,
    private _router: Router,
    private _http: HttpClient,
    private _localStorageService: LocalStorageService,
    private _jwtHelper: JwtHelperService
  ) {}

  public initAuthenticationForm(): FormGroup {
    return this._formBuilder.group({
      email: ['', [Validators.required, Validators.email, Validators.minLength(3)]],
      password: ['', [Validators.required, Validators.minLength(3)]]
    });
  }

  public authenticate(credentials: {email: string, password: string}): Observable<HttpResponse<{token: string}>> {
    this._loadingService.authentication$$.next(true);
    return this._http.post<{token: string}>('/token/generate-token', credentials, { observe: 'response' })
      .pipe(tap((response: HttpResponse<{token: string}>) => this._localStorageService.add(AuthenticationService.TOKEN, response.body.token)));
  }

  public canActivate(route: ActivatedRouteSnapshot, state: RouterStateSnapshot): Observable<boolean> | Promise<boolean> | boolean {
    const token: string = this._localStorageService.get(AuthenticationService.TOKEN);
    if (isNullOrUndefined(token) || this._jwtHelper.isTokenExpired(token)) {
      this.exit();
      return false;
    }

    if (this._profileService.isProfileLoaded()) {
      return true;
    }

    let authenticationCheck$$: Subject<boolean> = this._loadingService.authenticationCheck$$;
    authenticationCheck$$.next(true);
    const result: Subject<boolean> = new Subject<boolean>();
    this._profileService.loadFullProfile().subscribe(() => {
      authenticationCheck$$.next(false);
      result.next(true);
    });

    return result.asObservable();
  }

  public exit(): void {
    this._localStorageService.remove(AuthenticationService.TOKEN);
    this._profileService.clearProfile();
    this._router.navigate(['/authentication']);
  }
}
