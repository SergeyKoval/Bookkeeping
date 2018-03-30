import { Component, Inject } from '@angular/core';

import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material';

@Component({
  selector: 'bk-confirm-dialog',
  templateUrl: './confirm-dialog.component.html',
  styleUrls: ['./confirm-dialog.component.css']
})
export class ConfirmDialogComponent {
  public constructor(
    @Inject(MAT_DIALOG_DATA) public data: {title: string, body: string},
    public dialogRef: MatDialogRef<ConfirmDialogComponent>
  ) {}
}
