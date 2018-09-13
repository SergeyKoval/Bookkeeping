import { Injectable } from '@angular/core';
import { MatDialogRef } from '@angular/material';

import { ConfirmDialogComponent } from './confirm-dialog.component';
import { DialogService } from '../../service/dialog.service';

@Injectable()
export class ConfirmDialogService {

  public constructor(private _dialogService: DialogService) { }

  public openConfirmDialog(title: string, body: string): MatDialogRef<ConfirmDialogComponent> {
    return this._dialogService.openDialog(ConfirmDialogComponent, {
      data: {
        'title': title,
        'body': body,
      },
    });
  }

  public openConfirmDialogWithHtml(title: string, htmlBody: string): MatDialogRef<ConfirmDialogComponent> {
    return this._dialogService.openDialog(ConfirmDialogComponent, {
      data: {
        'title': title,
        'htmlBody': htmlBody,
      },
    });
  }
}
