import { Component, OnInit } from '@angular/core';
import { MatDialog } from '@angular/material';

import { Subscription ,  Observable } from 'rxjs';
import { filter, switchMap, tap } from 'rxjs/operators';

import { ProfileService } from '../../common/service/profile.service';
import { AlertService } from '../../common/service/alert.service';
import { ConfirmDialogService } from '../../common/components/confirm-dialog/confirm-dialog.service';
import { AlertType } from '../../common/model/alert/AlertType';
import { AccountDialogComponent } from './account-dialog/account-dialog.component';
import { BalanceDialogComponent } from './balance-dialog/balance-dialog.component';

@Component({
  selector: 'bk-accounts',
  templateUrl: './accounts.component.html',
  styleUrls: ['./accounts.component.css']
})
export class AccountsComponent implements OnInit {
  public loading: boolean = false;
  public profile: Profile;

  public constructor(
    private _profileService: ProfileService,
    private _dialog: MatDialog,
    private _alertService: AlertService,
    private _confirmDialogService: ConfirmDialogService
  ) { }

  public ngOnInit(): void {
    this.profile = this._profileService.authenticatedProfile;
    this.profile.accounts.forEach((account: FinAccount) => account.settingsOpened = false);
  }

  public addAccount(): void {
    this.openAccountDialog({
      'type': 'account',
      'editMode': false
    });
  }

  private openAccountDialog(dialogData: {}): void {
    const dialogResult: Observable<boolean> = this._dialog.open(AccountDialogComponent, {
      width: '550px',
      position: {top: 'top'},
      data: dialogData
    }).afterClosed();
    this.processAccountDialogResult(dialogResult);
  }

  private processAccountDialogResult(dialogResult: Observable<boolean>): void {
    dialogResult
      .pipe(
        filter((result: boolean) => result === true),
        tap(() => {
          this._alertService.addAlert(AlertType.SUCCESS, 'Операция успешно выполнена');
          this.loading = true;
        }),
        switchMap(() => this._profileService.reloadAccountsInProfile())
      ).subscribe(() => {
        this.loading = false;
    });
  }














  public hasSubAccounts(account: FinAccount): boolean {
    return account.subAccounts && account.subAccounts.length > 0;
  }

  public editAccount(editAccount: FinAccount): void {
    this.openAccountDialog({
      'type': 'account',
      'editMode': true,
      'account': editAccount.title
    });
  }

  public editSubAccountBalance(finAccount: FinAccount, subAccount: SubAccount): void {
    const subscription: Subscription = this._dialog.open(BalanceDialogComponent, {
      width: '350px',
      data: {'subAccountBalance': Object.assign([], subAccount.balance)}
    }).afterClosed().pipe(
      filter((result: BalanceItem[]) => result !== null),
      switchMap((result: BalanceItem[]) => this._profileService.reloadProfile())
    ).subscribe(() => {
      // const updatedAccounts: FinAccount[] = profiles[0].accounts;
      // updatedAccounts.forEach((account: FinAccount) => account.settingsOpened = this.getAccountOpened(this.accounts, account.title));
      // this.accounts = updatedAccounts;
      this.loading = false;
      this._alertService.addAlert(AlertType.SUCCESS, 'Операция успешно выполнена');
      subscription.unsubscribe();
    });
  }

  public addSubAccount(account: FinAccount): void {
    this.openAccountDialog({
      'type': 'subAccount',
      'editMode': false,
      'account': account.title
    });
  }

  public editSubAccount(account: FinAccount, subAccount: SubAccount): void {
    this.openAccountDialog({
      'type': 'subAccount',
      'editMode': true,
      'account': account.title,
      'subAccount': subAccount.title,
      'icon': subAccount.icon,
      'balance': subAccount.balance
    });
  }



  private getAccountOpened(accounts: FinAccount[], title: string): boolean {
    const element: FinAccount = accounts.filter((account: FinAccount) => account.title === title)[0];
    return element ? element.settingsOpened : false;
  }
}
