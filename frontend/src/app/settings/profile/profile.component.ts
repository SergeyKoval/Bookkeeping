import { Component, OnInit } from '@angular/core';
import { AbstractControl, FormControl, FormGroup } from '@angular/forms';

import { Store } from '@ngrx/store';

import { ProfileService } from '../../common/service/profile.service';
import { AlertType } from '../../common/model/alert/AlertType';
import * as fromUser from '../../common/redux/reducers/user';
import { UserActions } from '../../common/redux/actions';

@Component({
  selector: 'bk-profile',
  templateUrl: './profile.component.html',
  styleUrls: ['./profile.component.css']
})
export class ProfileComponent implements OnInit {
  public profileForm: FormGroup;
  public submitIndicator: boolean = false;

  public constructor(
    private _profileService: ProfileService,
    private _userStore: Store<fromUser.State>
  ) { }

  public ngOnInit(): void {
    this.profileForm = this._profileService.prepareProfileForm();
  }

  public changeOldPasswordValue(oldPassword: FormControl): void {
    const newPassword: AbstractControl = this.profileForm.controls.newPassword;
    if (oldPassword.value !== '') {
      newPassword.enable();
      if (newPassword.value !== '') {
        this.profileForm.controls.newPasswordAgain.enable();
      }
    } else {
      newPassword.disable();
      newPassword.setValue('');
      this.disableNewPasswordAgain();
    }
  }

  public changeNewPasswordValue(newPassword: FormControl): void {
    if (newPassword.value !== '' && this.profileForm.controls.oldPassword.value !== '') {
      this.profileForm.controls.newPasswordAgain.enable();
    } else {
      this.disableNewPasswordAgain();
    }
  }

  public submitProfile(): void {
    this.submitIndicator = true;
    this._profileService.updatePassword(this.profileForm.value).subscribe(response => {
      this.submitIndicator = false;
      if (response.status === 'FAIL') {
        const message: string = response.message === 'INVALID_PASSWORD' ? 'Неверный старый пароль' : 'Ошибка сервера';
        this._userStore.dispatch(UserActions.SHOW_ALERT({ alert: { type: AlertType.WARNING, message } }));
      } else {
        this._userStore.dispatch(UserActions.SHOW_ALERT({ alert: { type: AlertType.SUCCESS, message: 'Пороль успешно изменен' } }));
        this.profileForm.reset();
        this.profileForm.controls.newPassword.disable();
        this.disableNewPasswordAgain();
      }
    });
  }

  private disableNewPasswordAgain(): void {
    const newPasswordAgain: AbstractControl = this.profileForm.controls.newPasswordAgain;
    newPasswordAgain.disable();
    newPasswordAgain.setValue('');
  }
}
