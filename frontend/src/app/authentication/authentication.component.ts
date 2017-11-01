import { Component, OnInit } from '@angular/core';
import { FormGroup } from '@angular/forms';
import { Router } from '@angular/router';

import { Subscription } from 'rxjs/Subscription';

import { AuthenticationService } from '../common/service/authentication.service';
import { LoadingService } from '../common/service/loading.service';
import { CurrencyService } from '../common/service/currency.service';

@Component({
  selector: 'bk-authentication',
  templateUrl: './authentication.component.html',
  styleUrls: ['./authentication.component.css']
})
export class AuthenticationComponent implements OnInit {
  public authenticationForm: FormGroup;
  public submitted: boolean = false;
  public errorMessage: string;
  public loading: boolean = false;
  public applicationLoading: boolean = false;

  private _AUTHENTICATION_LOADING_SUBSCRIPTION: Subscription;

  public constructor(
    private _authenticationService: AuthenticationService,
    private _loadingService: LoadingService,
    private _currencyService: CurrencyService,
    private _router: Router
  ) {}

  public ngOnInit(): void {
    this.authenticationForm = this._authenticationService.initAuthenticationForm();
    this._AUTHENTICATION_LOADING_SUBSCRIPTION = this._loadingService.authentication$$.subscribe((value: boolean) => this.loading = value);
  }

  public authenticate(): void {
    this.submitted = true;

    if (!this.authenticationForm.valid) {
      return;
    }

    const subscription: Subscription = this._authenticationService.getProfileByEmail(this.authenticationForm.value.email).subscribe((profile: Profile) => {
      subscription.unsubscribe();

      if (!profile) {
        this.errorMessage = 'Несуществующий пользователь';
        return;
      }

      if (!this._authenticationService.authenticate(profile, this.authenticationForm.value.password)) {
        this.errorMessage = 'Неверный пароль';
        return;
      }

      this._AUTHENTICATION_LOADING_SUBSCRIPTION.unsubscribe();
      this.applicationLoading = true;
      this._router.navigate(['bookkeeping']);
      const currentDate: Date = new Date(Date.now());
      this._currencyService.loadCurrencies(currentDate.getUTCMonth() + 1, currentDate.getUTCFullYear(), this._authenticationService.getProfileCurrencies());
    });
  }
}
