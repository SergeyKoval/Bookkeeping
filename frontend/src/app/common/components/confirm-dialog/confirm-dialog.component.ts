import { Component, Inject } from '@angular/core';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';


@Component({
    selector: 'bk-confirm-dialog',
    templateUrl: './confirm-dialog.component.html',
    styleUrls: ['./confirm-dialog.component.css'],
    standalone: false
})
export class ConfirmDialogComponent {
  public constructor(
    @Inject(MAT_DIALOG_DATA) public data: {title: string, body: string, htmlBody: string},
    public dialogRef: MatDialogRef<ConfirmDialogComponent>
  ) {}
}
