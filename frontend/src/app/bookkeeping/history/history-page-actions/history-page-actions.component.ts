import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';

import {MdDialog} from '@angular/material';

import {HistoryEditPopupComponent} from '../history-edit-popup/history-edit-popup.component';

@Component({
  selector: 'bk-history-page-actions',
  templateUrl: './history-page-actions.component.html',
  styleUrls: ['./history-page-actions.component.css']
})
export class HistoryPageActionsComponent implements OnInit {
  @Input()
  public loading: boolean;
  @Input()
  public disableMoreButton: boolean;
  @Output()
  public loadMore: EventEmitter<null> = new EventEmitter();

  public constructor(private _dialog: MdDialog) { }

  public ngOnInit(): void {
  }

  public addHistoryItem(): void {
    this._dialog.open(HistoryEditPopupComponent, {
      width: '1000px',
      position: {top: 'top'},
      data: {
        'title': 'Новая операция',
        'historyItem': {date: Date.now()}
      },
    });
  }

  public showMoreHistoryItems(): void {
    if (!this.disableMoreButton) {
      this.loadMore.emit();
    }
  }

  public moreButtonDisabledValue(): string {
    return this.disableMoreButton ? 'true' : null;
  }
}
