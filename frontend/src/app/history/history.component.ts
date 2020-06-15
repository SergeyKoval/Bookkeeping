import { AfterViewChecked, Component, OnInit } from '@angular/core';
import { MatDialogRef } from '@angular/material';
import { MatDialog } from '@angular/material/dialog';

import { filter, switchMap, tap } from 'rxjs/operators';
import { of } from 'rxjs';

import { HistoryService } from '../common/service/history.service';
import { ProfileService } from '../common/service/profile.service';
import { ConfirmDialogService } from '../common/components/confirm-dialog/confirm-dialog.service';
import { HistoryItem } from '../common/model/history/HistoryItem';
import { AlertService } from '../common/service/alert.service';
import { AlertType } from '../common/model/alert/AlertType';
import { HistoryEditDialogComponent } from './history-edit-dialog/history-edit-dialog.component';
import { BudgetService } from '../common/service/budget.service';
import { CurrencyUtils } from '../common/utils/currency-utils';
import { LoadingService } from '../common/service/loading.service';
import { LoadingDialogComponent } from '../common/components/loading-dialog/loading-dialog.component';
import { SmsAssignDialogComponent } from './sms-assign-dialog/sms-assign-dialog.component';

@Component({
  selector: 'bk-history',
  templateUrl: './history.component.html',
  styleUrls: ['./history.component.css']
})
export class HistoryComponent implements OnInit, AfterViewChecked {
  public static readonly PAGE_LIMIT: number = 20;

  public loading: boolean = true;
  public loadingMoreIndicator: boolean = false;
  public disableMoreButton: boolean = false;
  public unprocessedSms: boolean = false;

  public historyItems: HistoryType[] = [];
  public unprocessedSmsCount: number;

  private _lastElementId: string;
  private _devices: {[deviceId: string]: Device};

  public constructor(
    private _dialog: MatDialog,
    private _historyService: HistoryService,
    private _authenticationService: ProfileService,
    private _confirmDialogService: ConfirmDialogService,
    private _alertService: AlertService,
    private _budgetService: BudgetService,
    private _profileService: ProfileService,
    private _loadingService: LoadingService
  ) {}

  public ngOnInit(): void {
    this._historyService.getUnprocessedSmsCount().subscribe(response => this.unprocessedSmsCount = response.result as number);
    this._devices = this._authenticationService.authenticatedProfile.devices;
    this.init(1, HistoryComponent.PAGE_LIMIT);
  }

  public ngAfterViewChecked(): void {
    if (!this.loading && this._lastElementId) {
      const htmlElement: HTMLElement = document.getElementById(this._lastElementId);
      if (htmlElement) {
        htmlElement.scrollIntoView();
      }
      this._lastElementId = null;
    }
  }

  public loadMoreItems(numberOfNewItems: number): void {
    if (numberOfNewItems > 1) {
      this._lastElementId = document.getElementsByClassName('last-item')[0].id;
    }
    this.init(1, this.historyItems.length + numberOfNewItems);
  }

  public editHistoryItem(historyItem: HistoryItem, fromSms: boolean): void {
    this._dialog.open(HistoryEditDialogComponent, {
      width: '720px',
      position: {top: 'top'},
      panelClass: 'history-add-edit-dialog',
      data: {
        'historyItem': historyItem.cloneOriginalItem(),
        'editMode': true,
        'fromSms': fromSms
      }
    }).afterClosed()
      .pipe(
        filter((result: boolean) => result === true),
        tap(() => this._lastElementId = historyItem.originalItem.id)
      ).subscribe(() => {
        this.loadMoreItems(0);
        if (fromSms) {
          this._historyService.getUnprocessedSmsCount().subscribe(response => this.unprocessedSmsCount = response.result as number);
        }
      });
  }

  public deleteHistoryItem(historyItem: HistoryItem): void {
    let loadingDialog: MatDialogRef<LoadingDialogComponent>;
    this._confirmDialogService.openConfirmDialog('Подтверждение', 'Точно удалить?')
      .afterClosed()
      .pipe(
        filter((result: boolean) => result === true),
        tap(() => loadingDialog = this._loadingService.openLoadingDialog('Проверка цели...')),
        switchMap(() => historyItem.goal ? this._budgetService.reviewBudgetGoalBeforeDelete(historyItem.originalItem.id, historyItem.originalItem.goal) : of({})),
        switchMap((simpleResponse: SimpleResponse) => {
          loadingDialog.close();
          if (simpleResponse.status === 'FAIL') {
            const result: {} = simpleResponse.result;
            return this._confirmDialogService.openConfirmDialogWithHtml('Изменение статуса цели',
              /* tslint:disable:max-line-length */
    `<div>Удаляемая операция является частью выполненной цели.</div>
              <div>После ее удаления прогресс цели будет <strong>${CurrencyUtils.convertCodeToSymbol(this._profileService.getCurrencyDetails(result['currency']).symbol)} ${result['value']} / ${result['completeValue']}.</strong></div>
              <div>Пометить цель невыполненной?</div>`).afterClosed();
              /* tslint:enable */
          } else {
            return of(false);
          }
        }),
        tap(result => loadingDialog = this._loadingService.openLoadingDialog('Удаление...')),
        switchMap(changeGoalStatus => this._historyService.deleteHistoryItem(historyItem.originalItem.id, changeGoalStatus)),
        tap(simpleResponse => {
          loadingDialog.close();
          if (simpleResponse.status === 'FAIL') {
            this._alertService.addAlert(AlertType.WARNING, 'Ошибка при удалении');
            this._lastElementId = historyItem.originalItem.id;
            this.loadMoreItems(0);
          }
        }),
        filter(simpleResponse => simpleResponse.status === 'SUCCESS')
      ).subscribe(() => {
        this._alertService.addAlert(AlertType.SUCCESS, 'Запись успешно удалена');
        this._authenticationService.quiteReloadAccounts();
        this.loadMoreItems(-1);
        this._historyService.getUnprocessedSmsCount().subscribe(response => this.unprocessedSmsCount = response.result as number);
      });
  }

  public changeShowSms(event: boolean): void {
    this.unprocessedSms = event;
    const moreItems: number = this.historyItems.length >= HistoryComponent.PAGE_LIMIT ? 0 : HistoryComponent.PAGE_LIMIT - this.historyItems.length;
    this.loadMoreItems(moreItems);
  }

  public getFormattedSms(sms: string): string {
    return sms.split('\n').join('<br>');
  }

  public getSmsDateTime(smsTimestamp: number): string {
    return new Date(smsTimestamp).toLocaleTimeString('ru-RU');
  }

  public getDeviceName(deviceId: string): string {
    const device: Device = this._devices[deviceId];
    return !device ? deviceId : device.name || deviceId;
  }

  public openAssignSmsDialog(historyItem: HistoryItem): void {
    this._dialog.open(SmsAssignDialogComponent, {
      width: '800px',
      position: {top: 'top'},
      panelClass: 'assign-sms-dialog',
      data: {
        'smsItem': historyItem.originalItem
      }
    }).afterClosed()
      .pipe(
        filter((result: boolean) => result === true),
        tap(() => this._lastElementId = null)
      ).subscribe(() => {
        this._historyService.getUnprocessedSmsCount().subscribe(response => this.unprocessedSmsCount = response.result as number);
        this.loadMoreItems(0);
      });
  }

  public clickOnSms(historyItem: HistoryItem, smsIndex: number): void {
    historyItem.showSmsIndex = historyItem.showSmsIndex !== smsIndex ? smsIndex : null;
  }

  private init(page: number, limit: number): void {
    this.loading = true;
    this._historyService.loadHistoryItems(page, limit, this.unprocessedSms).subscribe((historyItems: HistoryType[]) => {
      if (historyItems.length < limit) {
        this.disableMoreButton = true;
      }
      this.historyItems = historyItems;
      this.loading = false;
    });
  }
}
