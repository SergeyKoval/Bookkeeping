import { Component, OnInit } from '@angular/core';

import {MdDialogRef} from "@angular/material";

@Component({
  selector: 'bk-history-edit-popup',
  templateUrl: './history-edit-popup.component.html',
  styleUrls: ['./history-edit-popup.component.css']
})
export class HistoryEditPopupComponent implements OnInit {

  constructor(public dialogRef: MdDialogRef<HistoryEditPopupComponent>) { }

  ngOnInit() {
  }

}
