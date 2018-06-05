import { Component, OnInit } from '@angular/core';
import { MatDialog } from '@angular/material';

import { filter, switchMap, tap } from 'rxjs/operators';

import { UsersService } from '../common/service/users.service';
import { UserDialogComponent } from './user-dialog/user-dialog.component';
import { AlertType } from '../common/model/alert/AlertType';
import { AlertService } from '../common/service/alert.service';
import { ConfirmDialogService } from '../common/components/confirm-dialog/confirm-dialog.service';
import { ProfileService } from '../common/service/profile.service';

@Component({
  selector: 'bk-users',
  templateUrl: './users.component.html',
  styleUrls: ['./users.component.css']
})
export class UsersComponent implements OnInit {
  public loading: boolean = true;
  public users: User[];

  public constructor (
    private _dialog: MatDialog,
    private _usersService: UsersService,
    private _alertService: AlertService,
    private _confirmDialogService: ConfirmDialogService,
    private _profileService: ProfileService
  ) {}

  public ngOnInit(): void {
    this._usersService.getAllUsers().subscribe((users: User[]) => {
      this.loading = false;
      this.users = users;
    });
  }

  public addUser(): void {
    this.openUserDialog({editMode: false});
  }

  public editUser(user: User): void {
    this.openUserDialog({
      'editMode': true,
      'email': user.email,
      'roles': Object.assign([], user.roles)
    });
  }

  public deleteUser(user: User): void {
    this._confirmDialogService.openConfirmDialog('Подтверждение', 'При удалении пользователя будут удалены все связанные с ним операции')
      .afterClosed()
      .pipe(
        filter((result: boolean) => result === true),
        tap(() => this.loading = true),
        switchMap(() => this._usersService.deleteUser(user.email)),
        tap(simpleResponse => {
          if (simpleResponse.status === 'FAIL') {
            this._alertService.addAlert(AlertType.WARNING, 'Во время удаления произошла ошибка');
          }
        }),
        switchMap(() => this._usersService.getAllUsers())
      ).subscribe((users: User[]) => {
        this.users = users;
        this.loading = false;
      });
  }

  public isUser(user: User): boolean {
    return user.roles.indexOf('USER') > -1;
  }

  public isAdmin(user: User): boolean {
    return user.roles.indexOf('ADMIN') > -1;
  }

  public isCurrentUser(user: User): boolean {
    return this._profileService.authenticatedProfile.email === user.email;
  }

  private openUserDialog(dialogData: {editMode: boolean, email?: string, roles?: string[]}): void {
    this._dialog
      .open(UserDialogComponent, {
        width: '550px',
        position: {top: 'top'},
        data: dialogData
      })
      .afterClosed()
      .pipe(
        filter((result: boolean) => result === true),
        tap(() => {
          this.loading = true;
          this._alertService.addAlert(AlertType.SUCCESS, dialogData.editMode ? 'Пользователь успешно изменен' : 'Пользователь успешно добавлен');
        }),
        switchMap(() => this._usersService.getAllUsers())
      ).subscribe((users: User[]) => {
        this.users = users;
        this.loading = false;
      });
  }
}
