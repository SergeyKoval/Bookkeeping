import { Injectable } from '@angular/core';
import { MatDialogRef } from '@angular/material';
import { MatDialog } from '@angular/material/dialog';

import { ConfirmDialogComponent } from './confirm-dialog.component';

@Injectable()
export class ConfirmDialogService {

  public constructor(private _dialog: MatDialog) { }

  public openConfirmDialog(title: string, body: string): MatDialogRef<ConfirmDialogComponent> {
    return this._dialog.open(ConfirmDialogComponent, {
      id: 'confirm-dialog',
      data: {
        'title': title,
        'body': body,
      },
    });
  }

  public openConfirmDialogWithHtml(title: string, htmlBody: string): MatDialogRef<ConfirmDialogComponent> {
    return this._dialog.open(ConfirmDialogComponent, {
      id: 'confirm-dialog',
      data: {
        'title': title,
        'htmlBody': htmlBody,
      },
    });
  }
}
