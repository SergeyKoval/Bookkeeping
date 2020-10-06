import { Injectable } from '@angular/core';
import { Router } from '@angular/router';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { HttpClient, HttpResponse } from '@angular/common/http';
import { MatDialog } from '@angular/material/dialog';

import { LocalStorageService } from 'angular-2-local-storage';
import { Observable, Subject } from 'rxjs';
import { JwtHelperService } from '@auth0/angular-jwt';
import { tap } from 'rxjs/internal/operators';
import { Store } from '@ngrx/store';

import { LoadingService } from './loading.service';
import { ProfileService } from './profile.service';
import { CurrencyService } from './currency.service';
import { LoginPageActions, UserActions } from '../redux/actions';
import * as fromUser from '../redux/reducers/user';

@Injectable({
  providedIn: 'root'
})
export class AuthenticationService {
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
    private _dialogRef: MatDialog,
    private _store: Store<fromUser.State>,
    private _userStore: Store<fromUser.State>
  ) {}

  public initAuthenticationForm(): FormGroup {
    return this._formBuilder.group({
      email: ['', [Validators.required, Validators.email, Validators.minLength(3)]],
      password: ['', [Validators.required, Validators.minLength(3)]]
    });
  }

  public initRegistrationForm(restorePassword: boolean, email: string = '', password: string = ''): FormGroup {
    return this._formBuilder.group({
      email: [email, [Validators.required, Validators.email, Validators.minLength(3)]],
      password: [password, [Validators.required, Validators.minLength(3)]],
      restorePassword: [restorePassword]
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
    if (token !== null && token !== undefined) {
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
    if (token === null || token === undefined || this._jwtHelper.isTokenExpired(token)) {
      this.exit(token !== null);
      return false;
    }

    return true;
  }

  public exit(expiredSession: boolean = false): void {
    this._store.dispatch(UserActions.CLOSE_DIALOGS());
    this._localStorageService.remove(AuthenticationService.TOKEN);
    this._store.dispatch(LoginPageActions.LOGIN_REDIRECT({ expiredSession }));
  }

  public addErrorMessage(message: string): void {
    this._errorMessage$$.next(message);
    this._authentication$$.next(false);
    this._applicationLoading$$.next(false);
  }

  public get errorMessage$(): Observable<string> {
    return this._errorMessage$$.asObservable();
  }

  public getServerVersion(): Observable<SimpleResponse> {
    return this._http.get<SimpleResponse>(`/token/server/version?timestamp=${new Date().getTime()}`);
  }

  public closeAllDialogs(): void {
    this._dialogRef.closeAll();
  }

  public startAuthenticationExpirationJob(): void {
    if (this.validateToken()) {
      setTimeout(this.startAuthenticationExpirationJob.bind(this), 30000);
    } else {
      this.closeAllDialogs();
      console.log('Authentication expiration job execute logoff');
    }
  }
}
