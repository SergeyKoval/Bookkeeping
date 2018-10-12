import { Component, OnDestroy, OnInit, ViewChild } from '@angular/core';
import { FormBuilder, FormGroup } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';

import { Subscription } from 'rxjs';
import { switchMap } from 'rxjs/operators';

import { ProfileService } from '../common/service/profile.service';
import { LoadingService } from '../common/service/loading.service';
import { CurrencyService } from '../common/service/currency.service';
import { AuthenticationService } from '../common/service/authentication.service';
import { BrowserUtils } from '../common/utils/browser-utils';

@Component({
  selector: 'bk-authentication',
  templateUrl: './authentication.component.html',
  styleUrls: ['./authentication.component.css']
})
export class AuthenticationComponent implements OnInit, OnDestroy {
  public authenticationForm: FormGroup;
  public registrationForm: FormGroup;
  public submitted: boolean = false;
  public errorMessage: string;
  public loading: boolean = false;
  public applicationLoading: boolean = false;
  public alwaysEditing: boolean;
  public type: string = 'authentication';
  public codeSent: boolean = false;
  public unsupportedBrowser: boolean;

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
    private _route: ActivatedRoute,
    private _formBuilder: FormBuilder
  ) {}

  public ngOnInit(): void {
    this.unsupportedBrowser = !this.isBrowserSupported();
    this._route.queryParams.subscribe(value => {
      if (value.expiredSession === 'true') {
        this.errorMessage = 'Сессия устарела';
      }
    });
    this.authenticationForm = this._authenticationService.initAuthenticationForm();
    this._AUTHENTICATION_LOADING_SUBSCRIPTION = this._authenticationService.authentication$$.subscribe((value: boolean) => this.loading = value);
    this._APPLICATION_LOADING_SUBSCRIPTION = this._authenticationService.applicationLoading$$.subscribe((value: boolean) => this.applicationLoading = value);
    this._AUTHENTICATION_ERROR_SUBSCRIPTION = this._authenticationService.errorMessage$.subscribe(value => {
      if (value === 'MISSED USER') {
        this.errorMessage = 'Несуществующий пользователь';
        this.codeSent = false;
      } else if (value === 'BAD CREDENTIALS') {
        this.errorMessage = 'Неверный пароль';
      } else if (value === 'NOT ACTIVE') {
        this.showRegistrationForm(false, this.authenticationForm.get('email').value, this.authenticationForm.get('password').value);
        this.errorMessage = 'Пользователь не активирован';
        this.alwaysEditing = true;
      }

    });
    this.alwaysEditing = BrowserUtils.isMobileOrTablet();
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

    this._authenticationService.authenticate(this.authenticationForm.value).subscribe(() => {
      this.loadInitialData(['budget']);
    });
  }

  public showRegistrationForm(restorePassword: boolean, email: string = '', password: string = ''): void {
    this.errorMessage = null;
    this.submitted = false;
    this.registrationForm = this._authenticationService.initRegistrationForm(restorePassword, email, password);
    this.type = 'registration';
  }

  public sendCode(): void {
    this.errorMessage = null;
    this.submitted = true;
    if (!this.registrationForm.valid) {
      return;
    }

    if (!this.registrationForm.contains('code')) {
      this.registrationForm.addControl('code', this._formBuilder.control({value: '', disabled: false}));
    } else {
      this.registrationForm.patchValue({code: ''});
    }
    this.loading = true;
    this.codeSent = true;
    this._authenticationService.sendRegistrationCode(this.registrationForm.value).subscribe(response => {
      if (response.status === 'FAIL') {
        this.errorMessage = response.message === 'ALREADY_EXIST' ? 'Пользователь уже зарегистрирован' : 'Ошибка при отправке кода подтверждения';
      }
      this.loading = false;
    });
  }

  public reviewCode(): void {
    this.errorMessage = null;
    this.submitted = true;
    if (!this.registrationForm.valid) {
      return;
    }
    const codeValue: string = this.registrationForm.get('code').value;
    if (!codeValue || codeValue.length !== 4) {
      this.errorMessage = 'Код не совпадает';
      return;
    }


    this.loading = true;
    this._authenticationService.reviewRegistrationCode(this.registrationForm.value).subscribe(response => {
      this.loading = false;
      if (response.status === 'FAIL') {
        this.errorMessage = response.message === 'INVALID_CODE' ? 'Код не совпадает' : 'Ошибка при проверке кода';
      } else {
        this.loadInitialData(['settings', 'profile']);
      }
    });
  }

  public getBrowserWidth(): number {
    return window.innerWidth;
  }

  public isResolutionUnsupported(): boolean {
    return window.innerWidth < 768;
  }

  private isBrowserSupported(): boolean {
    // @ts-ignore
    const isChrome: boolean = !!window.chrome && !!window.chrome.webstore;
    console.log(isChrome);
    // @ts-ignore
    const isFirefox: boolean = typeof InstallTrigger !== 'undefined';
    console.log(isFirefox);
    return isChrome || isFirefox;
  }

  private loadInitialData(redirectPage: string[]): void {
    this.applicationLoading = true;
    this._profileService.loadFullProfile()
      .pipe(switchMap(() => this._currencyService.loadCurrenciesForCurrentMoth(this._profileService.getProfileCurrencies())))
      .subscribe(() => {
        this._profileService.initialDataLoaded = true;
        this._router.navigate(redirectPage);
      });
  }
}
