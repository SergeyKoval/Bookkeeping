import { Component, OnDestroy, OnInit, ViewChild } from '@angular/core';
import { FormGroup } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';

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
  public alwaysEditing: boolean;

  @ViewChild('formRef')
  private _FORM_REF: HTMLFormElement;
  private _AUTHENTICATION_LOADING_SUBSCRIPTION: Subscription;
  private _APPLICATION_LOADING_SUBSCRIPTION: Subscription;
  private _AUTHENTICATION_ERROR_SUBSCRIPTION: Subscription;

  public constructor(
    private _authenticationService: AuthenticationService,
    private _profileService: ProfileService,
    private _loadingService: LoadingService,
    private _currencyService: CurrencyService,
    private _router: Router,
    private _route: ActivatedRoute
  ) {}

  public ngOnInit(): void {
    this._route.queryParams.subscribe(value => {
      if (value.expiredSession === 'true') {
        this.errorMessage = 'Сессия устарела';
      }
    });
    this.authenticationForm = this._authenticationService.initAuthenticationForm();
    this._AUTHENTICATION_LOADING_SUBSCRIPTION = this._authenticationService.authentication$$.subscribe((value: boolean) => this.loading = value);
    this._APPLICATION_LOADING_SUBSCRIPTION = this._authenticationService.applicationLoading$$.subscribe((value: boolean) => this.applicationLoading = value);
    this._AUTHENTICATION_ERROR_SUBSCRIPTION = this._authenticationService.errorMessage$.subscribe(value => this.errorMessage = value);
    this.alwaysEditing = navigator.platform === 'iPad';
  }

  public ngOnDestroy(): void {
    if (this._AUTHENTICATION_LOADING_SUBSCRIPTION) {
      this._AUTHENTICATION_LOADING_SUBSCRIPTION.unsubscribe();
    }
    if (this._APPLICATION_LOADING_SUBSCRIPTION) {
      this._APPLICATION_LOADING_SUBSCRIPTION.unsubscribe();
    }
    if (this._AUTHENTICATION_ERROR_SUBSCRIPTION) {
      this._AUTHENTICATION_ERROR_SUBSCRIPTION.unsubscribe();
    }
  }

  public authenticate(): void {
    const formElement: HTMLCollection = this._FORM_REF.nativeElement.getElementsByTagName('input');
    const emailElement: HTMLInputElement = formElement.item(0) as HTMLInputElement;
    const passwordElement: HTMLInputElement = formElement.item(1) as HTMLInputElement;
    if (this.authenticationForm.get('email').value === '' && this.authenticationForm.get('password').value === ''
        && emailElement.value.length > 0 && passwordElement.value.length > 0) {
      this.authenticationForm.get('email').setValue(emailElement.value);
      this.authenticationForm.get('password').setValue(passwordElement.value);
    }

    this.submitted = true;
    if (!this.authenticationForm.valid) {
      return;
    }

    this._authenticationService.authenticate(this.authenticationForm.value)
      .pipe(
        tap(() => this.applicationLoading = true),
        switchMap(() => this._profileService.loadFullProfile()),
        switchMap(() => this._currencyService.loadCurrenciesForCurrentMoth(this._profileService.getProfileCurrencies()))
      ).subscribe(() => {
        this._profileService.initialDataLoaded = true;
        this._router.navigate(['budget']);
      });
  }
}
