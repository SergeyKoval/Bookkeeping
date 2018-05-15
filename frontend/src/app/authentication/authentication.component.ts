import { Component, OnInit } from '@angular/core';
import { FormGroup } from '@angular/forms';
import { Router } from '@angular/router';

import { Subscription } from 'rxjs';

import { ProfileService } from '../common/service/profile.service';
import { LoadingService } from '../common/service/loading.service';
import { CurrencyService } from '../common/service/currency.service';
import { AuthenticationService } from '../common/service/authentication.service';
import { HttpErrorResponse, HttpResponse } from '@angular/common/http';

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
    private _profileService: ProfileService,
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

    this._authenticationService.authenticate(this.authenticationForm.value)
      .subscribe(
        (response: HttpResponse<{token: string}>) => {
          this.applicationLoading = true;
          this._profileService.loadFullProfile().subscribe(() => {
            this._router.navigate(['budget']);
            const currentDate: Date = new Date(Date.now());
            this._currencyService.loadCurrencies(currentDate.getUTCMonth() + 1, currentDate.getUTCFullYear(), this._profileService.getProfileCurrencies());
          });
        },(errorResponse: HttpErrorResponse)  => {
          if (errorResponse.status === 401) {
            if (errorResponse.error === 'BAD CREDENTIALS') {
              this.errorMessage = 'Неверный пароль';
            }
          }
          this.loading = false;
        });
  }
}
