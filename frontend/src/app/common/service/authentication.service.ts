import { Injectable } from '@angular/core';
import { ActivatedRouteSnapshot, CanActivate, NavigationExtras, Router, RouterStateSnapshot } from '@angular/router';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { HttpClient, HttpResponse } from '@angular/common/http';

import { LocalStorageService } from 'angular-2-local-storage';
import { Observable, Subject } from 'rxjs/index';
import { JwtHelperService } from '@auth0/angular-jwt';
import { isNullOrUndefined } from 'util';
import { tap } from 'rxjs/internal/operators';
import { switchMap } from 'rxjs/operators';

import { LoadingService } from './loading.service';
import { ProfileService } from './profile.service';
import { CurrencyService } from './currency.service';
import { DialogService } from './dialog.service';

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
    private _jwtHelper: JwtHelperService,
    private _dialogService: DialogService
  ) {}

  public initAuthenticationForm(): FormGroup {
    return this._formBuilder.group({
      email: ['', [Validators.required, Validators.email, Validators.minLength(3)]],
      password: ['', [Validators.required, Validators.minLength(3)]]
    });
  }

  public initRegistrationForm(email: string, password: string): FormGroup {
    return this._formBuilder.group({
      email: [email, [Validators.required, Validators.email, Validators.minLength(3)]],
      password: [password, [Validators.required, Validators.minLength(3)]]
    });
  }

  public sendRegistrationCode(registrationData: {}): Observable<SimpleResponse> {
    return this._http.post<SimpleResponse>('/token/send-registration-code', registrationData);
  }

  public reviewRegistrationCode(registrationData: {}): Observable<SimpleResponse> {
    return this._http.post<SimpleResponse>('/token/review-registration-code', registrationData)
      .pipe(
        tap(response => {
          if (response.status === 'SUCCESS') {
            this._localStorageService.set(AuthenticationService.TOKEN, response.message);
            this.startAuthenticationExpirationJob();
          }
        }),
      );
  }

  public authenticate(credentials: {email: string, password: string}): Observable<HttpResponse<{token: string}>> {
    this._authentication$$.next(true);
    return this._http.post<{token: string}>('/token/generate-token', credentials, { observe: 'response' })
      .pipe(
        tap((response: HttpResponse<{token: string}>) => this._localStorageService.set(AuthenticationService.TOKEN, response.body.token)),
        tap(() => this.startAuthenticationExpirationJob())
      );
  }

  public refreshTokenIfRequired(): void {
    const token: string = this._localStorageService.get(AuthenticationService.TOKEN);
    if (!isNullOrUndefined(token)) {
      const now: Date = new Date();
      const tokenExpirationDate: Date = this._jwtHelper.getTokenExpirationDate(token);
      if ((tokenExpirationDate.getTime() - now.getTime()) < 300000) {
        this._http.post<{token: string}>('/token/refresh-token', {'token': token})
          .subscribe((response: {token: string}) => this._localStorageService.set(AuthenticationService.TOKEN, response.token));
      }
    }
  }

  public validateToken(): boolean {
    const token: string = this._localStorageService.get(AuthenticationService.TOKEN);
    if (isNullOrUndefined(token) || this._jwtHelper.isTokenExpired(token)) {
      this.exit();
      return false;
    }

    return true;
  }

  public canActivate(route: ActivatedRouteSnapshot, state: RouterStateSnapshot): Observable<boolean> | Promise<boolean> | boolean {
    if (!this.validateToken()) {
      return false;
    }

    if (this._profileService.initialDataLoaded === true) {
      return true;
    }

    const authenticationCheck$$: Subject<boolean> = this._loadingService.authenticationCheck$$;
    authenticationCheck$$.next(true);
    const result: Subject<boolean> = new Subject<boolean>();
    this._profileService.loadFullProfile()
      .pipe(switchMap(() => this._currencyService.loadCurrenciesForCurrentMoth(this._profileService.getProfileCurrencies())))
      .subscribe(() => {
        authenticationCheck$$.next(false);
        result.next(true);
        this._profileService.initialDataLoaded = true;
        this.startAuthenticationExpirationJob();
    });

    return result.asObservable();
  }

  public exit(expiredSession: boolean = false): void {
    this._profileService.initialDataLoaded = false;
    this._localStorageService.remove(AuthenticationService.TOKEN);
    this._profileService.clearProfile();
    this._authentication$$.next(false);
    this._applicationLoading$$.next(false);
    const navigationExtras: NavigationExtras = expiredSession ? {queryParams: {expiredSession: true}} : {};
    this._router.navigate(['/authentication'], navigationExtras);
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

  private startAuthenticationExpirationJob(): void {
    if (this.validateToken()) {
      setTimeout(this.startAuthenticationExpirationJob.bind(this), 30000);
    } else {
      this._dialogService.closeAllDialogs();
      console.log('Authentication expiration job execute logoff');
    }
  }
}
