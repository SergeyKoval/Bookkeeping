import { Component, Inject } from '@angular/core';
import { MAT_DIALOG_DATA, MatDialog, MatDialogRef } from '@angular/material/dialog';

@Component({
  selector: 'bk-device-name-dialog',
  templateUrl: './device-name-dialog.component.html',
  styleUrls: ['./device-name-dialog.component.css']
})
export class DeviceNameDialogComponent {

  public constructor(
    @Inject(MAT_DIALOG_DATA) public data: {name: string, deviceId: string},
    private _dialogRef: MatDialogRef<DeviceNameDialogComponent>,
    private _dialog: MatDialog
  ) { }

  public save(): void {
    this._dialogRef.close(this.data);
  }

  public close(): void {
    this._dialogRef.close(null);
  }
}
