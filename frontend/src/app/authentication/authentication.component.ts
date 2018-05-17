import { Component, OnDestroy, OnInit } from '@angular/core';
import { FormGroup } from '@angular/forms';
import { Router } from '@angular/router';

import { Subscription } from 'rxjs';
import { tap } from 'rxjs/internal/operators';
import { switchMap } from 'rxjs/operators';

import { ProfileService } from '../common/service/profile.service';
import { LoadingService } from '../common/service/loading.service';
import { CurrencyService } from '../common/service/currency.service';
import { AuthenticationService } from '../common/service/authentication.service';

@Component({
  selector: 'bk-authentication',
  templateUrl: './authentication.component.html',
  styleUrls: ['./authentication.component.css']
})
export class AuthenticationComponent implements OnInit, OnDestroy {
  public authenticationForm: FormGroup;
  public submitted: boolean = false;
  public errorMessage: string;
  public loading: boolean = false;
  public applicationLoading: boolean = false;

  private _AUTHENTICATION_LOADING_SUBSCRIPTION: Subscription;
  private _APPLICATION_LOADING_SUBSCRIPTION: Subscription;
  private _AUTHENTICATION_ERROR_SUBSCRIPTION: Subscription;

  public constructor(
    private _authenticationService: AuthenticationService,
    private _profileService: ProfileService,
    private _loadingService: LoadingService,
    private _currencyService: CurrencyService,
    private _router: Router
  ) {}

  public ngOnInit(): void {
    this.authenticationForm = this._authenticationService.initAuthenticationForm();
    this._AUTHENTICATION_LOADING_SUBSCRIPTION = this._authenticationService.authentication$$.subscribe((value: boolean) => this.loading = value);
    this._APPLICATION_LOADING_SUBSCRIPTION = this._authenticationService.applicationLoading$$.subscribe((value: boolean) => this.applicationLoading = value);
    this._AUTHENTICATION_ERROR_SUBSCRIPTION = this._authenticationService.errorMessage$.subscribe(value => this.errorMessage = value);
  }

  public ngOnDestroy(): void {
    this._AUTHENTICATION_LOADING_SUBSCRIPTION.unsubscribe();
    this._APPLICATION_LOADING_SUBSCRIPTION.unsubscribe();
    this._AUTHENTICATION_ERROR_SUBSCRIPTION.unsubscribe();
  }

  public authenticate(): void {
    this.submitted = true;

    if (!this.authenticationForm.valid) {
      return;
    }

    this._authenticationService.authenticate(this.authenticationForm.value)
      .pipe(
        tap(() => this.applicationLoading = true),
        switchMap(() => this._profileService.loadFullProfile()),
        switchMap(() => {
          const currentDate: Date = new Date(Date.now());
          const currenciesRequest: {month: number, year: number, currencies: string[]} = {
            month: currentDate.getUTCMonth() + 1,
            year: currentDate.getUTCFullYear(),
            currencies: this._profileService.getProfileCurrencies()
          };
          return this._currencyService.loadCurrenciesForMonth(currenciesRequest);
        })
      ).subscribe(() => {
        this._profileService.initialDataLoaded = true;
        this._router.navigate(['budget']);
      });
  }
}
