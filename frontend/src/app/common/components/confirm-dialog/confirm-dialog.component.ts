import {Component, Inject} from '@angular/core';

import {MD_DIALOG_DATA, MdDialogRef} from '@angular/material';

@Component({
  selector: 'bk-confirm-dialog',
  templateUrl: './confirm-dialog.component.html',
  styleUrls: ['./confirm-dialog.component.css']
})
export class ConfirmDialogComponent {
  public constructor(
    @Inject(MD_DIALOG_DATA) public data: {title: string, body: string},
    public dialogRef: MdDialogRef<ConfirmDialogComponent>
  ) {}
}
