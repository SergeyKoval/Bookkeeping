import { Component, Inject, OnInit } from '@angular/core';
import { MAT_DIALOG_DATA, MatDialog, MatDialogRef } from '@angular/material/dialog';
import { FormControl, FormGroup, Validators } from '@angular/forms';

@Component({
  selector: 'bk-device-mail-dialog',
  templateUrl: './device-mail-dialog.component.html',
  styleUrls: ['./device-mail-dialog.component.css']
})
export class DeviceMailDialogComponent implements OnInit {
  public emailForm: FormGroup;
  public showErrorMessage: boolean = false;

  public constructor(
    @Inject(MAT_DIALOG_DATA) private _data: string,
    private _dialogRef: MatDialogRef<DeviceMailDialogComponent>,
    private _dialog: MatDialog
  ) { }

  public ngOnInit (): void {
    this.emailForm = new FormGroup({
      email: new FormControl(this._data, [Validators.required, Validators.email])
    });
  }

  public save(): void {
    this.showErrorMessage = !this.emailForm.valid;
    if (!this.showErrorMessage) {
      this._dialogRef.close(this.emailForm.value);
    }
  }

  public close(): void {
    this._dialogRef.close(null);
  }
}
