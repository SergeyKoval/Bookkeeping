import { Injectable } from '@angular/core';

import {MdDialog, MdDialogRef} from '@angular/material';

import {ConfirmDialogComponent} from './confirm-dialog.component';

@Injectable()
export class ConfirmDialogService {

  public constructor(private _dialog: MdDialog) { }

  public openConfirmDialog(title: string, body: string): MdDialogRef<ConfirmDialogComponent> {
    return this._dialog.open(ConfirmDialogComponent, {
      data: {
        'title': title,
        'body': body
      },
    });
  }
}
