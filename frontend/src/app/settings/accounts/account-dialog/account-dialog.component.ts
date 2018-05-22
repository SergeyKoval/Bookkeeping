import { Component, Inject, OnInit } from '@angular/core';
import { MAT_DIALOG_DATA, MatDialog, MatDialogRef } from '@angular/material';

import { Subscription } from 'rxjs';
import { filter } from 'rxjs/operators';

import { ProfileService } from '../../../common/service/profile.service';
import { BalanceDialogComponent } from '../balance-dialog/balance-dialog.component';

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
    @Inject(MAT_DIALOG_DATA) public data: {editMode: boolean, type: string, account: string, subAccount: string, icon: string, balance: BalanceItem[]},
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

  public save(): void {
    this.loading = true;
    if (!this.data.editMode) {
      this._profileService.addAccount(this.title).subscribe(result => {
        this.loading = false;
        if (result.status === 'FAIL') {
          this.errorMessage = result.message === 'ALREADY_EXIST' ? 'Счет с таким названием уже существует': 'Ошибка при добавлении счета';
          return;
        }

        this._dialogRef.close(true);
      });
    } else if (this.title === this.data.account) {
      this._dialogRef.close(false);
    } else {
      this._profileService.editAccount(this.data.account, this.title).subscribe(result => {
        this.loading = false;
        if (result.status === 'FAIL') {
          this.errorMessage = result.message === 'ALREADY_EXIST' ? 'Счет с таким названием уже существует': 'Ошибка при добавлении счета';
          return;
        }

        this._dialogRef.close(true);
      });
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

  public editSubAccountBalance(): void {
    const subscription: Subscription = this._dialog.open(BalanceDialogComponent, {
      width: '350px',
      data: {'subAccountBalance': Object.assign([], this.data.balance)}
    }).afterClosed().pipe(
      filter((result: BalanceItem[]) => result !== null),
    ).subscribe((result: BalanceItem[]) => {
      console.log(result);
      subscription.unsubscribe();
    });
  }
}
