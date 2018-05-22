import { Component, OnInit } from '@angular/core';
import { MatDialog } from '@angular/material';

import { Subscription ,  Observable } from 'rxjs';
import { filter, switchMap, tap } from 'rxjs/operators';
import { Subject } from 'rxjs/index';

import { ProfileService } from '../../common/service/profile.service';
import { AlertService } from '../../common/service/alert.service';
import { ConfirmDialogService } from '../../common/components/confirm-dialog/confirm-dialog.service';
import { AlertType } from '../../common/model/alert/AlertType';
import { AccountDialogComponent } from './account-dialog/account-dialog.component';
import { BalanceDialogComponent } from './balance-dialog/balance-dialog.component';
import { LoadingService } from '../../common/service/loading.service';

@Component({
  selector: 'bk-accounts',
  templateUrl: './accounts.component.html',
  styleUrls: ['./accounts.component.css']
})
export class AccountsComponent implements OnInit {
  public loading: boolean = false;
  public profile: Profile;

  private _ACCOUNTS_LOADING: Subject<boolean>;

  public constructor(
    private _loadingService: LoadingService,
    private _profileService: ProfileService,
    private _dialog: MatDialog,
    private _alertService: AlertService,
    private _confirmDialogService: ConfirmDialogService
  ) { }

  public ngOnInit(): void {
    this._ACCOUNTS_LOADING = this._loadingService.accounts$$;
    this.profile = this._profileService.authenticatedProfile;
    this.profile.accounts.forEach((account: FinAccount) => account.settingsOpened = false);
  }

  public addAccount(): void {
    this.openAccountDialog({
      'type': 'account',
      'editMode': false
    });
  }

  public editAccount(editAccount: FinAccount): void {
    this.openAccountDialog({
      'type': 'account',
      'editMode': true,
      'account': editAccount.title
    });
  }

  public deleteAccount(deleteAccount: FinAccount): void {
    this._confirmDialogService.openConfirmDialog('Подтверждение', 'При удалении счета все существующие операции с использованием этого счета будут удалены. Остатки на удаляемом счете будут утерены. Продолжить?')
      .afterClosed()
      .pipe(
        filter((result: boolean) => result === true),
        tap(() => {
          this.loading = true;
          this._ACCOUNTS_LOADING.next(true);
        }),
        switchMap(() => this._profileService.deleteAccount(deleteAccount.title)),
        tap(simpleResponse => {
          if (simpleResponse.status === 'FAIL') {
            this._alertService.addAlert(AlertType.WARNING, 'Во время удаления произошла ошибка');
          } else {
            this._alertService.addAlert(AlertType.SUCCESS, 'Счет успешно удален');
          }
        }),
        switchMap(() => this._profileService.reloadAccountsInProfile())
      ).subscribe(() => {
        this._ACCOUNTS_LOADING.next(false);
        this.loading = false;
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
          this._ACCOUNTS_LOADING.next(true);
        }),
        switchMap(() => this._profileService.reloadAccountsInProfile())
      ).subscribe(() => {
        this.loading = false;
        this._ACCOUNTS_LOADING.next(false);
    });
  }














  public hasSubAccounts(account: FinAccount): boolean {
    return account.subAccounts && account.subAccounts.length > 0;
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
