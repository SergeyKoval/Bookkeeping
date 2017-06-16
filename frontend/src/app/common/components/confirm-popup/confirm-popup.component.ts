import {Component, Inject, OnInit} from '@angular/core';

import {MD_DIALOG_DATA, MdDialogRef} from '@angular/material';

@Component({
  selector: 'bk-confirm-popup',
  templateUrl: './confirm-popup.component.html',
  styleUrls: ['./confirm-popup.component.css']
})
export class ConfirmPopupComponent {

  public constructor(
    @Inject(MD_DIALOG_DATA) public data: {title: string, body: string},
    public dialogRef: MdDialogRef<ConfirmPopupComponent>
  ) {}
}
