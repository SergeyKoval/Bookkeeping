import { ChangeDetectionStrategy, Component, Input, OnInit, ViewChild } from '@angular/core';
import { FormGroup } from '@angular/forms';

import { Store } from '@ngrx/store';

import { AuthenticationService } from '../../../common/service/authentication.service';
import { fromLoginPage } from '../../../common/redux/reducers';
import { LoginPageActions } from '../../../common/redux/actions';

@Component({
  selector: 'bk-authentication',
  templateUrl: './authentication.component.html',
  styleUrls: ['./authentication.component.css'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class AuthenticationComponent implements OnInit {
  @Input()
  public alwaysEditing: boolean;
  @Input()
  public authenticationChecking: boolean;
  @Input()
  public formSubmitted: boolean;

  public authenticationForm: FormGroup;

  @ViewChild('formRef', {static : false})
  private _FORM_REF: HTMLFormElement;

  public constructor(
    private _store: Store<fromLoginPage.LoginPageState>,
    private _authenticationService: AuthenticationService
  ) { }

  public ngOnInit() {
    this.authenticationForm = this._authenticationService.initAuthenticationForm();
  }

  public showRegistrationForm(restorePasswordForm: boolean): void {
    this._store.dispatch(LoginPageActions.SHOW_REGISTRATION_FORM({ restorePasswordForm }));
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

    this._store.dispatch(LoginPageActions.FORM_PRE_VALIDATION());
    if (!this.authenticationForm.valid) {
      return;
    }

    this._store.dispatch(LoginPageActions.AUTHENTICATE({ credentials: this.authenticationForm.value }));
  }
}
