import { Component, Inject, OnInit } from '@angular/core';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';

import { DateUtils } from '../../common/utils/date-utils';
import { HistoryService } from '../../common/service/history.service';
import { ProfileService } from '../../common/service/profile.service';
import { LoadingService } from '../../common/service/loading.service';
import { LoadingDialogComponent } from '../../common/components/loading-dialog/loading-dialog.component';
import { AlertService } from '../../common/service/alert.service';
import { AlertType } from '../../common/model/alert/AlertType';
import { DeviceMessage } from '../../common/model/history/deviceMessage';
import { HistoryType } from '../../common/model/history/history-type';
import { HistoryBalanceType } from '../../common/model/history/history-balance-type';
import { SimpleResponse } from '../../common/model/simple-response';

@Component({
    selector: 'bk-device-message-assign-dialog',
    templateUrl: './device-message-assign-dialog.component.html',
    styleUrls: ['./device-message-assign-dialog.component.css'],
    standalone: false
})
export class DeviceMessageAssignDialogComponent implements OnInit {
  public loading: boolean = true;
  public previousAvailable: boolean = true;
  public nextAvailable: boolean = true;
  public deviceMessage: DeviceMessage;
  public year: number;
  public month: number;
  public day: number;
  public historyItems: HistoryType[];

  public constructor (
    @Inject(MAT_DIALOG_DATA) public data: {deviceMessageItem: HistoryType},
    private _dialogRef: MatDialogRef<DeviceMessageAssignDialogComponent>,
    private _historyService: HistoryService,
    private _profileService: ProfileService,
    private _loadingService: LoadingService,
    private _alertService: AlertService
  ) { }

  public ngOnInit(): void {
    this.deviceMessage = this.data.deviceMessageItem.deviceMessages[0];
    this.year = this.data.deviceMessageItem.year;
    this.month = this.data.deviceMessageItem.month;
    this.day = this.data.deviceMessageItem.day;
    this._historyService.getDayHistoryItems(this.year, this.month, this.day).subscribe(response => {
      this.historyItems = response.result as HistoryType[];
      this.loading = false;
    });
  }

  public getFormattedDeviceMessage(): string {
    return this.deviceMessage.fullText.split('\n').join('<br>');
  }

  public getSelectedDate(): string {
    return DateUtils.convertDateToString(this.year, this.month, this.day);
  }

  public getAccountIcon(balance: HistoryBalanceType): string {
    return this._profileService.getAccountIcon(balance.account, balance.subAccount);
  }

  public getCategoryIcon (historyItem: HistoryType): string {
    return this._profileService.getCategoryIcon(historyItem.category);
  }

  public assignWithHistoryItem(historyItem: HistoryType): void {
    const loadingDialog: MatDialogRef<LoadingDialogComponent> = this._loadingService.openLoadingDialog('Ассоциация сообщения с операцией...');
    this._historyService.assignDeviceMessageWithHistoryItem(this.data.deviceMessageItem.id, historyItem.id).subscribe(response => {
      loadingDialog.close();
      if (response.status === 'SUCCESS') {
        this._alertService.addAlert(AlertType.SUCCESS, 'Сообщение успешно ассоциирована с операцией');
        this._dialogRef.close(true);
      } else {
        this._alertService.addAlert(AlertType.WARNING, 'Не удалось ассоциировать сообщение с операцией');
      }
    });
  }

  public switchToPreviousDate(): void {
    if (!this.previousAvailable) {
      return;
    }

    this.loading = true;
    this._historyService.getDayHistoryItems(this.year, this.month, this.day, 'previous').subscribe(response => this.processDaySwitch(response, false));
  }

  public switchToNextDate(): void {
    if (!this.nextAvailable) {
      return;
    }

    this.loading = true;
    this._historyService.getDayHistoryItems(this.year, this.month, this.day, 'next').subscribe(response => this.processDaySwitch(response, true));
  }

  public close(): void {
    this._dialogRef.close(false);
  }

  private processDaySwitch(response: SimpleResponse, nextOperation: boolean): void {
    if (response.status === 'SUCCESS') {
      const dayItems: HistoryType[] = response.result as HistoryType[];
      if (dayItems && dayItems.length > 0) {
        nextOperation ? this.previousAvailable = true : this.nextAvailable = true;
        this.historyItems = dayItems;
        const dayItem: HistoryType = dayItems[0];
        this.year = dayItem.year;
        this.month = dayItem.month;
        this.day = dayItem.day;
      }
    } else if (response.message === 'MISSED') {
      nextOperation ? this.nextAvailable = false : this.previousAvailable = false;
    }
    this.loading = false;
  }
}
