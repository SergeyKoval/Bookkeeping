import { Component, Inject, OnInit } from '@angular/core';
import { MAT_DIALOG_DATA, MatDialog, MatDialogRef } from '@angular/material';

import { filter } from 'rxjs/operators';

import { ProfileService } from '../../../common/service/profile.service';
import { BalanceDialogComponent } from '../balance-dialog/balance-dialog.component';
import { Observable } from 'rxjs/index';

@Component({
  selector: 'bk-account-dialog',
  templateUrl: './account-dialog.component.html',
  styleUrls: ['./account-dialog.component.css']
})
export class AccountDialogComponent implements OnInit {
  public accountIcons: string[] = ['kwallet.gif', 'Money.gif', 'home.gif', 'other_exp.gif'];
  public title: string;
  public errorMessage: string;
  public loading: boolean;

  public constructor(
    @Inject(MAT_DIALOG_DATA) public data: {editMode: boolean, type: string, account: string, subAccount: string, icon: string, balance: {[currency: string]: number}},
    private _dialogRef: MatDialogRef<AccountDialogComponent>,
    private _profileService: ProfileService,
    private _dialog: MatDialog
  ) { }

  public ngOnInit(): void {
    if (this.data.editMode) {
      this.title = this.isAccount() ? this.data.account : this.data.subAccount;
    }
    if (!this.data.editMode && !this.isAccount()) {
      this.data.icon = this.accountIcons[0];
    }
  }

  public editSubAccountBalance(): void {
    this._dialog.open(BalanceDialogComponent, {
      width: '350px',
      data: {'subAccountBalance': Object.assign({}, this.data.balance)}
    }).afterClosed()
      .pipe(filter((result: {[currency: string]: number}) => result !== null))
      .subscribe((result: {[currency: string]: number}) => {
        this.data.balance = result;
    });
  }

  public save(): void {
    this.loading = true;

    if(this.data.type === 'account') {
      if (!this.data.editMode) {
        this.processResult(this._profileService.addAccount(this.title), 'Счет с таким названием уже существует', 'Ошибка при добавлении счета');
      } else if (this.title === this.data.account) {
        this._dialogRef.close(false);
      } else {
        this.processResult(this._profileService.editAccount(this.data.account, this.title), 'Счет с таким названием уже существует', 'Ошибка при изминении счета');
      }
    }

    if(this.data.type === 'subAccount') {
      if (!this.data.editMode) {
        this.processResult(this._profileService.addSubAccount(this.title, this.data.account, this.data.icon, this.data.balance), 'Субчет с таким названием уже существует', 'Ошибка при добавлении субсчета');
      } else {
        this.processResult(this._profileService.editSubAccount(this.data.account, this.data.subAccount, this.title, this.data.icon, this.data.balance), 'Субсчет с таким названием уже существует', 'Ошибка при изменении субсчета');
      }
    }
  }

  public isAccount(): boolean {
    return this.data.type === 'account';
  }

  public close(dialogResult: boolean): void {
    this._dialogRef.close(dialogResult);
  }

  public chooseIcon(icon: string): void {
    this.data.icon = icon;
  }

  private processResult(resultObservable: Observable<SimpleResponse>, alreadyExistError: string, otherError: string): void {
    resultObservable.subscribe(result => {
      this.loading = false;
      if (result.status === 'FAIL') {
        this.errorMessage = result.message === 'ALREADY_EXIST' ? alreadyExistError: otherError;
        return;
      }

      this._dialogRef.close(true);
    });
  }
}
