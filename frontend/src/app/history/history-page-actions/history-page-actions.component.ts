import { Component, EventEmitter, Input, Output } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';

import { filter } from 'rxjs/operators';

import { HistoryEditDialogComponent } from '../history-edit-dialog/history-edit-dialog.component';
import { HistoryComponent } from '../history.component';

@Component({
  selector: 'bk-history-page-actions',
  templateUrl: './history-page-actions.component.html',
  styleUrls: ['./history-page-actions.component.css']
})
export class HistoryPageActionsComponent {
  @Input()
  public loading: boolean;
  @Input()
  public disableMoreButton: boolean;
  @Input()
  public unprocessedSms: boolean;
  @Input()
  public unprocessedSmsCount: number;
  @Output()
  public loadMore: EventEmitter<number> = new EventEmitter();
  @Output()
  public onShowUnprocessedSmsChanged: EventEmitter<boolean> = new EventEmitter();

  public constructor(private _dialog: MatDialog) { }

  public addHistoryItem(): void {
    this._dialog.open(HistoryEditDialogComponent, {
      width: '720px',
      position: {top: 'top'},
      panelClass: 'history-add-edit-dialog',
      data: {
        'historyItem': null,
        'editMode': false
      },
    }).afterClosed()
      .pipe(filter((result: boolean) => result === true))
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

  public showUnprocessedSms(value: boolean): void {
    this.onShowUnprocessedSmsChanged.next(value);
  }
}
