import { AfterViewChecked, Component, OnInit } from '@angular/core';
import { MatDialog, MatDialogRef } from '@angular/material/dialog';

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
import { DeviceMessageAssignDialogComponent } from './device-message-assign-dialog/device-message-assign-dialog.component';
import { HistoryType } from '../common/model/history/history-type';
import { Device } from '../common/model/device';
import { SimpleResponse } from '../common/model/simple-response';

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
  public unprocessedDeviceMessages: boolean = false;

  public historyItems: HistoryType[] = [];
  public unprocessedDeviceMessagesCount: number;

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
    this._historyService.getUnprocessedDeviceMessagesCount().subscribe(response => this.unprocessedDeviceMessagesCount = response.result as number);
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

  public editHistoryItem(historyItem: HistoryItem, fromDeviceMessage: boolean): void {
    this._dialog.open(HistoryEditDialogComponent, {
      width: '800px',
      position: {top: 'top'},
      panelClass: 'history-add-edit-dialog',
      data: {
        'historyItem': historyItem.cloneOriginalItem(),
        'editMode': true,
        'fromDeviceMessage': fromDeviceMessage
      }
    }).afterClosed()
      .pipe(
        filter((result: boolean) => result === true),
        tap(() => this._lastElementId = historyItem.originalItem.id)
      ).subscribe(() => {
        this.loadMoreItems(0);
        if (fromDeviceMessage) {
          this._historyService.getUnprocessedDeviceMessagesCount().subscribe(response => this.unprocessedDeviceMessagesCount = response.result as number);
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
        this._historyService.getUnprocessedDeviceMessagesCount().subscribe(response => this.unprocessedDeviceMessagesCount = response.result as number);
      });
  }

  public changeShowDeviceMessages(event: boolean): void {
    this.unprocessedDeviceMessages = event;
    const moreItems: number = this.historyItems.length >= HistoryComponent.PAGE_LIMIT ? 0 : HistoryComponent.PAGE_LIMIT - this.historyItems.length;
    this.loadMoreItems(moreItems);
  }

  public getFormattedDeviceMessage(deviceMessage: string): string {
    return deviceMessage.split('\n').join('<br>');
  }

  public getDeviceMessageDateTime(deviceMessageTimestamp: number): string {
    return new Date(deviceMessageTimestamp).toLocaleTimeString('ru-RU');
  }

  public getDeviceName(deviceId: string): string {
    const device: Device = this._devices[deviceId];
    return !device ? deviceId : device.name || deviceId;
  }

  public openAssignDeviceMessageDialog(historyItem: HistoryItem): void {
    this._dialog.open(DeviceMessageAssignDialogComponent, {
      width: '800px',
      position: {top: 'top'},
      panelClass: 'assign-device-message-dialog',
      data: {
        'deviceMessageItem': historyItem.originalItem
      }
    }).afterClosed()
      .pipe(
        filter((result: boolean) => result === true),
        tap(() => this._lastElementId = null)
      ).subscribe(() => {
        this._historyService.getUnprocessedDeviceMessagesCount().subscribe(response => this.unprocessedDeviceMessagesCount = response.result as number);
        this.loadMoreItems(0);
      });
  }

  public clickOnDeviceMessage(historyItem: HistoryItem, deviceMessageIndex: number): void {
    historyItem.showDeviceMessageIndex = historyItem.showDeviceMessageIndex !== deviceMessageIndex ? deviceMessageIndex : null;
  }

  private init(page: number, limit: number): void {
    this.loading = true;
    this._historyService.loadHistoryItems(page, limit, this.unprocessedDeviceMessages).subscribe((historyItems: HistoryType[]) => {
      if (historyItems.length < limit) {
        this.disableMoreButton = true;
      }
      this.historyItems = historyItems;
      this.loading = false;
    });
  }
}
