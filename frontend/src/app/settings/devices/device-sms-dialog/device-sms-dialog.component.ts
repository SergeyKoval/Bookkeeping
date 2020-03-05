import { Component, Inject, OnInit } from '@angular/core';
import { MAT_DIALOG_DATA, MatDialog, MatDialogRef } from '@angular/material/dialog';

import { ProfileService } from '../../../common/service/profile.service';
import { AlertService } from '../../../common/service/alert.service';
import { AlertType } from '../../../common/model/alert/AlertType';

@Component({
  selector: 'bk-device-sms-dialog',
  templateUrl: './device-sms-dialog.component.html',
  styleUrls: ['./device-sms-dialog.component.css']
})
export class DeviceSmsDialogComponent implements OnInit {
  public loading: boolean = true;
  public sms: Sms;
  private smsIndex: number;
  private maxSmsIndex: number;

  public constructor(
    @Inject(MAT_DIALOG_DATA) public data: {name: string, deviceId: string},
    private _dialogRef: MatDialogRef<DeviceSmsDialogComponent>,
    private _dialog: MatDialog,
    private _profileService: ProfileService,
    private _alertService: AlertService
  ) { }

  public ngOnInit(): void {
    this.maxSmsIndex = null;
    this.smsIndex = 0;
    this.loadSms(0);
  }

  public loadSms(indexAction: number): void {
    this.loading = true;
    this._profileService.getDeviceSms(this.data.deviceId, this.smsIndex + indexAction).subscribe(response => {
      if (response.status === 'SUCCESS') {
        this.sms = response.result as Sms;
        this.smsIndex = this.smsIndex + indexAction;
      } else if (response.message === 'MISSED') {
        this.maxSmsIndex = this.smsIndex;
        this._alertService.addAlert(AlertType.INFO, 'Больше нет sms для данного девайса');
      } else {
        this._alertService.addAlert(AlertType.DANGER, 'Ошибка при получении Sms');
      }

      this.loading = false;
    });
  }

  public getSmsTimestamp(): string {
    const date: Date = new Date(this.sms.smsTimestamp);
    return `${date.toLocaleDateString('ru-RU')} ${date.toLocaleTimeString('ru-RU')}`;
  }

  public getSubstitutedFullSmsText(): string {
    return this.sms.fullSms.split('\n').join('<br>');
  }

  public close(): void {
    this._dialogRef.close();
  }
}
