import { Component, Inject, OnInit } from '@angular/core';
import { MAT_DIALOG_DATA, MatDialog, MatDialogRef } from '@angular/material/dialog';

import { ProfileService } from '../../../common/service/profile.service';
import { AlertService } from '../../../common/service/alert.service';
import { AlertType } from '../../../common/model/alert/AlertType';
import { DeviceMessage } from '../../../common/model/history/deviceMessage';

@Component({
  selector: 'bk-device-message-dialog',
  templateUrl: './device-message-dialog.component.html',
  styleUrls: ['./device-message-dialog.component.css']
})
export class DeviceMessageDialogComponent implements OnInit {
  public loading: boolean = true;
  public deviceMessage: DeviceMessage;
  public deviceMessageIndex: number;
  public maxDeviceMessageIndex: number;

  public constructor(
    @Inject(MAT_DIALOG_DATA) public data: {name: string, deviceId: string},
    private _dialogRef: MatDialogRef<DeviceMessageDialogComponent>,
    private _dialog: MatDialog,
    private _profileService: ProfileService,
    private _alertService: AlertService
  ) { }

  public ngOnInit(): void {
    this.maxDeviceMessageIndex = null;
    this.deviceMessageIndex = 0;
    this.loadDeviceMessages(0);
  }

  public loadDeviceMessages(indexAction: number): void {
    this.loading = true;
    this._profileService.getDeviceMessage(this.data.deviceId, this.deviceMessageIndex + indexAction).subscribe(response => {
      if (response.status === 'SUCCESS') {
        this.deviceMessage = response.result as DeviceMessage;
        this.deviceMessageIndex = this.deviceMessageIndex + indexAction;
      } else if (response.message === 'MISSED') {
        this.maxDeviceMessageIndex = this.deviceMessageIndex;
        this._alertService.addAlert(AlertType.INFO, 'Больше нет сообщений для данного девайса');
      } else {
        this._alertService.addAlert(AlertType.DANGER, 'Ошибка при получении сообщений');
      }

      this.loading = false;
    });
  }

  public getDeviceMessageTimestamp(): string {
    const date: Date = new Date(this.deviceMessage.messageTimestamp);
    return `${date.toLocaleDateString('ru-RU')} ${date.toLocaleTimeString('ru-RU')}`;
  }

  public getSubstitutedFullDeviceMessageText(): string {
    return this.deviceMessage.fullText.split('\n').join('<br>');
  }

  public close(): void {
    this._dialogRef.close();
  }
}
