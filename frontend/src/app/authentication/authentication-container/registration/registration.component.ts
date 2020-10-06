import { ChangeDetectionStrategy, Component, Input, OnInit } from '@angular/core';
import { FormBuilder, FormGroup } from '@angular/forms';

import { select, Store } from '@ngrx/store';
import { Observable } from 'rxjs';
import { filter } from 'rxjs/operators';

import { LoginPageActions } from '../../../common/redux/actions';
import { AuthenticationService } from '../../../common/service/authentication.service';
import { FormState } from '../../../common/redux/reducers/pages/login-page.reducer';
import { fromLoginPage } from '../../../common/redux/reducers';

@Component({
  selector: 'bk-registration',
  templateUrl: './registration.component.html',
  styleUrls: ['./registration.component.css'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class RegistrationComponent implements OnInit {
  @Input()
  public alwaysEditing: boolean;
  @Input()
  public formSubmitted: boolean;
  @Input()
  public authenticationChecking: boolean;

  public formState$: Observable<FormState>;
  public registrationForm: FormGroup;

  public constructor(
    private _store: Store<fromLoginPage.LoginPageState>,
    private _authenticationService: AuthenticationService,
    private _formBuilder: FormBuilder
  ) { }

  public ngOnInit() {
    this.registrationForm = this._authenticationService.initRegistrationForm(true);
    this.formState$ = this._store.pipe(select(fromLoginPage.selectFormState));
    this.formState$
      .pipe(filter(state => this.registrationForm === null || state.restorePasswordForm !== this.registrationForm.get('restorePassword').value))
      .subscribe(state => this.registrationForm = this._authenticationService.initRegistrationForm(state.restorePasswordForm, state.email, state.password));
  }

  public showAuthenticationForm(): void {
    this._store.dispatch(LoginPageActions.SHOW_AUTHENTICATION_FORM());
  }

  public sendCode(): void {
    this._store.dispatch(LoginPageActions.FORM_PRE_VALIDATION());
    if (!this.registrationForm.valid) {
      return;
    }

    if (!this.registrationForm.contains('code')) {
      this.registrationForm.addControl('code', this._formBuilder.control({value: '', disabled: false}));
    } else {
      this.registrationForm.patchValue({code: ''});
    }

    this._store.dispatch(LoginPageActions.SEND_CODE({ registrationData: this.registrationForm.value }));
  }

  public reviewCode(): void {
    this._store.dispatch(LoginPageActions.FORM_PRE_VALIDATION());
    if (!this.registrationForm.valid) {
      return;
    }

    const codeValue: string = this.registrationForm.get('code').value;
    if (!codeValue || codeValue.length !== 4) {
      this._store.dispatch(LoginPageActions.ADD_ERROR_MESSAGE({ message: 'Код не совпадает' }));
      return;
    }

    this._store.dispatch(LoginPageActions.REVIEW_CODE({ registrationData: this.registrationForm.value }));
  }
}
