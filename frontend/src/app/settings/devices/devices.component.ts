import { Component, OnInit } from '@angular/core';
import { MatDialog, MatDialogRef } from '@angular/material/dialog';

import * as fileSaver from 'file-saver';
import { filter } from 'rxjs/operators';

import { ProfileService } from '../../common/service/profile.service';
import { DeviceMailDialogComponent } from './device-mail-dialog/device-mail-dialog.component';
import { LoadingService } from '../../common/service/loading.service';
import { LoadingDialogComponent } from '../../common/components/loading-dialog/loading-dialog.component';
import { AlertService } from '../../common/service/alert.service';
import { AlertType } from '../../common/model/alert/AlertType';
import { DeviceNameDialogComponent } from './device-name-dialog/device-name-dialog.component';
import { DeviceSmsDialogComponent } from './device-sms-dialog/device-sms-dialog.component';

@Component({
  selector: 'bk-devices',
  templateUrl: './devices.component.html',
  styleUrls: ['./devices.component.css']
})
export class DevicesComponent implements OnInit {
  public devices: {[deviceId: string]: Device};

  public constructor(
    private _profileService: ProfileService,
    private _dialog: MatDialog,
    private _loadingService: LoadingService,
    private _alertService: AlertService
  ) { }

  public ngOnInit(): void {
    this.devices = this._profileService.authenticatedProfile.devices;
  }

  public downloadApplication(): void {
    const loadingDialog: MatDialogRef<LoadingDialogComponent> = this._loadingService.openLoadingDialog('Загрузка приложения...');
    this._profileService.downloadAndroidApplication().subscribe(file => {
      loadingDialog.close();
      if (file) {
        fileSaver.saveAs(file, `bookkeeper.apk`);
      }
    });
  }

  public openSendEmailDialog(): void {
    this._dialog.open(DeviceMailDialogComponent, {
      width: '550px',
      position: {top: 'top'},
      data: this._profileService.authenticatedProfile.email
    })
      .afterClosed()
      .pipe(filter((result: {}) => result !== null))
      .subscribe(email => {
        const loadingDialog: MatDialogRef<LoadingDialogComponent> = this._loadingService.openLoadingDialog('Отправка мэйла...');
        this._profileService.sendApplicationEmail(email).subscribe(response => {
          loadingDialog.close();
          if (response.status === 'SUCCESS') {
            this._alertService.addAlert(AlertType.SUCCESS, 'Ссылка на приложение отправлена');
          } else {
            this._alertService.addAlert(AlertType.WARNING, 'Произошла ошибка при отправке письма');
          }
        });
      });
  }

  public openChangeNameDialog(device: Device, deviceId: string): void {
    this._dialog.open(DeviceNameDialogComponent, {
      width: '550px',
      position: {top: 'top'},
      data: {'name': device.name, 'deviceId': deviceId}
    })
      .afterClosed()
      .pipe(filter((result: {}) => result !== null))
      .subscribe(deviceDetails => {
        const loadingDialog: MatDialogRef<LoadingDialogComponent> = this._loadingService.openLoadingDialog('Изминение имени...');
        this._profileService.changeDeviceName(deviceDetails).subscribe(response => {
          this._profileService.reloadDevicesInProfile().subscribe(profile => {
            this.devices = profile.devices;
            loadingDialog.close();
            if (response.status === 'SUCCESS') {
              this._alertService.addAlert(AlertType.SUCCESS, 'Имя девайса изменено');
            } else {
              this._alertService.addAlert(AlertType.WARNING, 'Произошла ошибка при изменении имени девайса');
            }
          });
        });
      });
  }

  public openProcessedSmsDialog(device: Device, deviceId: string): void {
    this._dialog.open(DeviceSmsDialogComponent, {
      width: '550px',
      position: {top: 'top'},
      data: {'name': device.name, 'deviceId': deviceId}
    })
      .afterClosed()
      .subscribe();
  }
}
