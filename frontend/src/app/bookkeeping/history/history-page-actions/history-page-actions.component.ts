import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';

import {MdDialog} from '@angular/material';

import {HistoryEditPopupComponent} from '../history-edit-popup/history-edit-popup.component';
import {HistoryComponent} from '../history.component';

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
  public loadMore: EventEmitter<number> = new EventEmitter();

  public constructor(private _dialog: MdDialog) { }

  public ngOnInit(): void {
  }

  public addHistoryItem(): void {
    this._dialog.open(HistoryEditPopupComponent, {
      width: '850px',
      position: {top: 'top'},
      data: {
        'title': 'Новая операция',
        'historyItem': null
      },
    }).afterClosed()
      .filter((result: boolean) => result === true)
      .subscribe(() => this.loadMore.emit(1));
  }

  public showMoreHistoryItems(): void {
    if (!this.disableMoreButton) {
      this.loadMore.emit(HistoryComponent.PAGE_LIMIT);
    }
  }

  public moreButtonDisabledValue(): string {
    return this.disableMoreButton ? 'true' : null;
  }
}
