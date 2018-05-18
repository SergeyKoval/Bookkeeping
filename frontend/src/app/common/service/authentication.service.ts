import { Injectable } from '@angular/core';
import { ActivatedRouteSnapshot, CanActivate, Router, RouterStateSnapshot } from '@angular/router';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { HttpClient, HttpResponse } from '@angular/common/http';

import { LocalStorageService } from 'angular-2-local-storage';
import { Observable, Subject } from 'rxjs/index';
import { JwtHelperService } from '@auth0/angular-jwt';
import { isNullOrUndefined } from "util";
import { tap } from 'rxjs/internal/operators';
import { switchMap } from 'rxjs/operators';

import { LoadingService } from './loading.service';
import { ProfileService } from './profile.service';
import { CurrencyService } from './currency.service';

@Injectable({
  providedIn: 'root'
})
export class AuthenticationService implements CanActivate {
  public static readonly TOKEN: string = 'access_token';

  private _errorMessage$$: Subject<string> = new Subject<string>();
  private _authentication$$: Subject<boolean> = new Subject();
  private _applicationLoading$$: Subject<boolean> = new Subject();

  public constructor(
    private _currencyService: CurrencyService,
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
    this._authentication$$.next(true);
    return this._http.post<{token: string}>('/token/generate-token', credentials, { observe: 'response' })
      .pipe(tap((response: HttpResponse<{token: string}>) => this._localStorageService.set(AuthenticationService.TOKEN, response.body.token)));
  }

  public canActivate(route: ActivatedRouteSnapshot, state: RouterStateSnapshot): Observable<boolean> | Promise<boolean> | boolean {
    const token: string = this._localStorageService.get(AuthenticationService.TOKEN);
    if (isNullOrUndefined(token) || this._jwtHelper.isTokenExpired(token)) {
      this.exit();
      return false;
    }

    if (this._profileService.initialDataLoaded === true) {
      return true;
    }

    let authenticationCheck$$: Subject<boolean> = this._loadingService.authenticationCheck$$;
    authenticationCheck$$.next(true);
    const result: Subject<boolean> = new Subject<boolean>();
    this._profileService.loadFullProfile()
      .pipe(switchMap(() => {
        const currentDate: Date = new Date(Date.now());
        const currenciesRequest: {month: number, year: number, currencies: string[]} = {
          month: currentDate.getUTCMonth() + 1,
          year: currentDate.getUTCFullYear(),
          currencies: this._profileService.getProfileCurrencies()
        };
        return this._currencyService.loadCurrenciesForMonth(currenciesRequest);
      }))
      .subscribe(() => {
        authenticationCheck$$.next(false);
        result.next(true);
        this._profileService.initialDataLoaded = true;
    });

    return result.asObservable();
  }

  public exit(): void {
    this._profileService.initialDataLoaded = false;
    this._localStorageService.remove(AuthenticationService.TOKEN);
    this._profileService.clearProfile();
    this._authentication$$.next(false);
    this._applicationLoading$$.next(false);
    this._router.navigate(['/authentication']);
  }

  public addErrorMessage(message: string): void {
    this._errorMessage$$.next(message);
    this._authentication$$.next(false);
    this._applicationLoading$$.next(false);
  }

  public get errorMessage$(): Observable<string> {
    return this._errorMessage$$.asObservable();
  }

  public get authentication$$(): Subject<boolean> {
    return this._authentication$$;
  }


  public get applicationLoading$$(): Subject<boolean> {
    return this._applicationLoading$$;
  }
}
