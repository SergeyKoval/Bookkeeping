import { Injectable } from '@angular/core';

import {MdDialog, MdDialogRef} from '@angular/material';

import {ConfirmPopupComponent} from './confirm-popup.component';

@Injectable()
export class ConfirmPopupService {

  public constructor(private dialog: MdDialog) { }

  public openConfirmPopup(title: string, body: string): MdDialogRef<ConfirmPopupComponent> {
    return this.dialog.open(ConfirmPopupComponent, {
      data: {
        'title': title,
        'body': body
      },
    });
  }
}
