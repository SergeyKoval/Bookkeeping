import { Component, Inject, OnInit } from '@angular/core';
import { MAT_DIALOG_DATA, MatDialog, MatDialogRef } from '@angular/material';

import { Observable } from 'rxjs/index';

import { UsersService } from '../../common/service/users.service';

@Component({
  selector: 'bk-user-dialog',
  templateUrl: './user-dialog.component.html',
  styleUrls: ['./user-dialog.component.css']
})
export class UserDialogComponent implements OnInit {
  public loading: boolean = false;
  public email: string;
  public roles: string[];
  public errorMessage: string;
  public password: string;

  public constructor(
    @Inject(MAT_DIALOG_DATA) public data: {email: string, roles: string[], editMode: boolean},
    private _dialogRef: MatDialogRef<UserDialogComponent>,
    private _dialog: MatDialog,
    private _usersService: UsersService
  ) { }

  public ngOnInit(): void {
    if (this.data.editMode) {
      this.email = this.data.email;
      this.roles = this.data.roles;
    } else {
      this.roles = ['USER'];
    }
  }

  public save(emailInput: HTMLInputElement, passwordInput: HTMLInputElement): void {
    this.errorMessage = null;
    if (!this.data.editMode && !emailInput.checkValidity()) {
      this.errorMessage = 'Мэил задан неверно';
      return;
    }

    if (((this.data.editMode && passwordInput.value.length > 0) || !this.data.editMode) && !passwordInput.checkValidity()) {
      this.errorMessage = 'Пароль задан неверно';
      return;
    }

    this.loading = true;
    if (this.data.editMode) {
      this.processResult(this._usersService.editUser(this.email, this.password, this.roles));
    } else {
      this.processResult(this._usersService.addUser(this.email, this.password, this.roles));
    }
  }

  public close(dialogResult: boolean): void {
    this._dialogRef.close(dialogResult);
  }

  public isAdmin(): boolean {
    return this.roles.indexOf('ADMIN') > -1;
  }

  public changeAdminState(): void {
    const adminIndex: number = this.roles.indexOf('ADMIN');
    if (adminIndex > -1) {
      this.roles.splice(adminIndex, 1);
    } else {
      this.roles.push('ADMIN');
    }
  }

  private processResult(resultObservable: Observable<SimpleResponse>): void {
    resultObservable
      .subscribe(result => {
        this.loading = false;
        if (result.status === 'FAIL') {
          this.errorMessage = result.message === 'ALREADY_EXIST' ? 'Пользователь с таким мэйлом уже существует' : 'Ошибка при сохранении';
          return;
        }

        this._dialogRef.close(true);
      });
  }
}
