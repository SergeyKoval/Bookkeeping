import { Component, Inject, OnInit } from '@angular/core';
import { MAT_DIALOG_DATA, MatDialog, MatDialogRef } from '@angular/material';

import { Subscription } from 'rxjs/Subscription';
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
