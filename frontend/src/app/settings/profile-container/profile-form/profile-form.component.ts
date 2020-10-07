import { ChangeDetectionStrategy, Component, Input, OnDestroy, OnInit } from '@angular/core';
import { AbstractControl, FormGroup } from '@angular/forms';

import { select, Store } from '@ngrx/store';
import { filter } from 'rxjs/operators';
import { Subscription } from 'rxjs';

import { SettingsProfilePageActions } from '../../../common/redux/actions';
import { ProfileService } from '../../../common/service/profile.service';
import * as fromUser from '../../../common/redux/reducers/user';
import { fromSettingsPage } from '../../../common/redux/reducers';

@Component({
  selector: 'bk-profile-form',
  templateUrl: './profile-form.component.html',
  styleUrls: ['./profile-form.component.css'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class ProfileFormComponent implements OnInit, OnDestroy {
  @Input()
  public profileEmail: string;
  @Input()
  public submitIndicator: boolean;

  public profileForm: FormGroup;
  private _resetFormSubscription: Subscription;

  public constructor (
    private _profileService: ProfileService,
    private _userStore: Store<fromUser.State>,
    private _store: Store<fromSettingsPage.State>
  ) {}

  public ngOnInit (): void {
    this.profileForm = this._profileService.prepareProfileForm(this.profileEmail);
    this._resetFormSubscription = this._store.pipe(
      select(fromSettingsPage.selectProfileResetForm),
      filter(resetForm => !!resetForm)
    ).subscribe(() => this.resetForm());
  }

  public ngOnDestroy (): void {
    this._resetFormSubscription.unsubscribe();
  }

  public changeOldPasswordValue(oldPassword: AbstractControl): void {
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

  public changeNewPasswordValue(newPassword: AbstractControl): void {
    if (newPassword.value !== '' && this.profileForm.controls.oldPassword.value !== '') {
      this.profileForm.controls.newPasswordAgain.enable();
    } else {
      this.disableNewPasswordAgain();
    }
  }

  public submitProfile(): void {
    this._userStore.dispatch(SettingsProfilePageActions.UPDATE_PASSWORD(this.profileForm.value));
  }

  private resetForm(): void {
    this.profileForm.reset();
    this.profileForm.get('email').patchValue(this.profileEmail);
    this.profileForm.controls.newPassword.disable();
    this.disableNewPasswordAgain();
  }

  private disableNewPasswordAgain(): void {
    const newPasswordAgain: AbstractControl = this.profileForm.controls.newPasswordAgain;
    newPasswordAgain.disable();
    newPasswordAgain.setValue('');
  }
}
