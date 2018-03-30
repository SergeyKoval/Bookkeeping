import { Injectable } from '@angular/core';

import { MatDialog, MatDialogRef } from '@angular/material';

import { ConfirmDialogComponent } from './confirm-dialog.component';

@Injectable()
export class ConfirmDialogService {

  public constructor(private _dialog: MatDialog) { }

  public openConfirmDialog(title: string, body: string): MatDialogRef<ConfirmDialogComponent> {
    return this._dialog.open(ConfirmDialogComponent, {
      data: {
        'title': title,
        'body': body
      },
    });
  }
}
