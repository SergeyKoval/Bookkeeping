import { Injectable } from '@angular/core';
import { MatDialog, MatDialogRef } from '@angular/material';
import { ComponentType } from '@angular/cdk/portal';
import { MatDialogConfig } from '@angular/material/dialog/typings/dialog-config';

@Injectable({
  providedIn: 'root'
})
export class DialogService {
  private _OPENED_DIALOGS: MatDialogRef<any, any>[] = [];

  public constructor(private _dialog: MatDialog) {}

  public openDialog<T, D = any, R = any>(componentRef: ComponentType<T>, config?: MatDialogConfig<D>): MatDialogRef<T, R> {
    let matDialogRef: MatDialogRef<T, R> = this._dialog.open(componentRef, config);
    this._OPENED_DIALOGS.push(matDialogRef);
    matDialogRef.afterClosed().subscribe(() => {
      this._OPENED_DIALOGS.forEach(dialog => {
        if (!dialog._overlayRef.hostElement) {
          this._OPENED_DIALOGS.splice(this._OPENED_DIALOGS.indexOf(dialog), 1);
        }
      });
    });

    return matDialogRef;
  }

  public closeAllDialogs(): void {
    this._OPENED_DIALOGS.forEach(dialog => {
      dialog.close();
      this._OPENED_DIALOGS.splice(this._OPENED_DIALOGS.indexOf(dialog), 1);
    });
  }
}
