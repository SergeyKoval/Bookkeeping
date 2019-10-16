import { Component, OnInit } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';

import { Observable, of, Subject } from 'rxjs';
import { filter, switchMap, tap } from 'rxjs/operators';

import { ProfileService } from '../../common/service/profile.service';
import { AlertService } from '../../common/service/alert.service';
import { ConfirmDialogService } from '../../common/components/confirm-dialog/confirm-dialog.service';
import { AlertType } from '../../common/model/alert/AlertType';
import { BalanceDialogComponent } from './balance-dialog/balance-dialog.component';
import { LoadingService } from '../../common/service/loading.service';
import { AccountCategoryDialogComponent } from '../account-category-dialog/account-category-dialog.component';

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

  public hasSubAccounts(account: FinAccount): boolean {
    return account.subAccounts && account.subAccounts.length > 0;
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
      'title': editAccount.title
    });
  }

  public deleteAccount(deleteAccount: FinAccount): void {
    let removeCallback = () => this._profileService.deleteAccount(deleteAccount.title);
    this.deleteAccountOrSubAccount(removeCallback, 'При удалении счета все существующие операции с использованием этого счета будут удалены. Остатки на удаляемом счете будут утерены. Продолжить?');
  }

  public moveAccountDown(account: FinAccount): void {
    this.loading = true;
    this._ACCOUNTS_LOADING.next(true);
    this.moveAccountOrSubAccount(this._profileService.moveAccountDown(account.title));
  }

  public moveAccountUp(account: FinAccount): void {
    this.loading = true;
    this._ACCOUNTS_LOADING.next(true);
    this.moveAccountOrSubAccount(this._profileService.moveAccountUp(account.title));
  }

  public addSubAccount(account: FinAccount): void {
    this.openAccountDialog({
      'type': 'subAccount',
      'editMode': false,
      'parentTitle': account.title,
      'balance': {}
    });
  }

  public editSubAccountBalance(finAccount: FinAccount, subAccount: SubAccount): void {
    this._dialog.open(BalanceDialogComponent, {
      id: 'balance-dialog',
      position: {top: 'top'},
      width: '350px',
      data: {'subAccountBalance': Object.assign({}, subAccount.balance)}
    }).afterClosed()
      .pipe(
        filter((result: {[currency: string]: number}) => result !== null),
        tap(() => {
          this.loading = true;
          this._ACCOUNTS_LOADING.next(true);
        }),
        switchMap((result: {[currency: string]: number}) => this._profileService.changeSubAccountBalance(finAccount.title, subAccount.title, result)),
        tap(simpleResponse => {
            if (simpleResponse.status === 'FAIL') {
              this._alertService.addAlert(AlertType.WARNING, 'Во время сохранения произошла ошибка');
            } else {
              this._alertService.addAlert(AlertType.SUCCESS, 'Операция успешно выполнена');
            }
        }),
        switchMap(() => this._profileService.reloadAccountsInProfile())
    ).subscribe(() => {
      this.loading = false;
      this._ACCOUNTS_LOADING.next(false);
    });
  }

  public editSubAccount(account: FinAccount, subAccount: SubAccount): void {
    this.openAccountDialog({
      'type': 'subAccount',
      'editMode': true,
      'parentTitle': account.title,
      'title': subAccount.title,
      'icon': subAccount.icon,
      'balance': subAccount.balance
    });
  }

  public moveSubAccountUp(account: FinAccount, subAccount: SubAccount): void {
    this.loading = true;
    this._ACCOUNTS_LOADING.next(true);
    this.moveAccountOrSubAccount(this._profileService.moveSubAccountUp(account.title, subAccount.title));
  }

  public moveSubAccountDown(account: FinAccount, subAccount: SubAccount): void {
    this.loading = true;
    this._ACCOUNTS_LOADING.next(true);
    this.moveAccountOrSubAccount(this._profileService.moveSubAccountDown(account.title, subAccount.title));
  }

  public deleteSubAccount(account: FinAccount, subAccount: SubAccount): void {
    let removeCallback = () => this._profileService.deleteSubAccount(account.title, subAccount.title);
    this.deleteAccountOrSubAccount(removeCallback, 'При удалении субсчета все существующие операции с использованием этого субсчета будут удалены. Остатки на удаляемом субсчете будут утерены. Продолжить?');
  }

  private deleteAccountOrSubAccount(removeCallback: () => Observable<SimpleResponse>, message: string): void {
    const dialogResult: Observable<boolean> = this._confirmDialogService.openConfirmDialog('Подтверждение', message)
      .afterClosed()
      .pipe(
        filter((result: boolean) => result === true),
        tap(() => {
          this.loading = true;
          this._ACCOUNTS_LOADING.next(true);
        }),
        switchMap(removeCallback),
        tap(simpleResponse => {
          if (simpleResponse.status === 'FAIL') {
            this._alertService.addAlert(AlertType.WARNING, 'Во время удаления произошла ошибка');
          }
        }),
        switchMap(simpleResponse => simpleResponse.status === 'SUCCESS' ? of(true) : of(false))
      );
    this.processAccountDialogResult(dialogResult);
  }

  private moveAccountOrSubAccount(result: Observable<SimpleResponse>): void {
    const moveResult: Observable<boolean> = result
      .pipe(
        tap(simpleResponse => {
          if (simpleResponse.status === 'FAIL') {
            this._alertService.addAlert(AlertType.WARNING, 'Во время перемещения произошла ошибка');
          }
        }),
        switchMap(simpleResponse => simpleResponse.status === 'SUCCESS' ? of(true) : of(false))
      );
    this.processAccountDialogResult(moveResult);
  }

  private openAccountDialog(dialogData: {}): void {
    const dialogResult: Observable<boolean> = this._dialog.open(AccountCategoryDialogComponent, {
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
}
