import { Component, OnInit } from '@angular/core';
import { AbstractControl, FormControl, FormGroup } from '@angular/forms';

import { ProfileService } from '../../common/service/profile.service';

@Component({
  selector: 'bk-profile',
  templateUrl: './profile.component.html',
  styleUrls: ['./profile.component.css']
})
export class ProfileComponent implements OnInit {
  public profileForm: FormGroup;

  public constructor(private _profileService: ProfileService) { }

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
    console.log('submit');
  }

  private disableNewPasswordAgain(): void {
    const newPasswordAgain: AbstractControl = this.profileForm.controls.newPasswordAgain;
    newPasswordAgain.disable();
    newPasswordAgain.setValue('');
  }
}
