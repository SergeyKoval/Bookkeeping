import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { MatSlideToggleChange } from '@angular/material/slide-toggle';

import { filter } from 'rxjs/operators';

import { HistoryEditDialogComponent } from '../history-edit-dialog/history-edit-dialog.component';
import { HistoryComponent } from '../history.component';

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
  @Input()
  public showSms: boolean;
  @Output()
  public loadMore: EventEmitter<number> = new EventEmitter();
  @Output()
  public onShowSmsChanged: EventEmitter<boolean> = new EventEmitter();

  public constructor(private _dialog: MatDialog) { }

  public ngOnInit(): void {
  }

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

  public changeShowSms(event: MatSlideToggleChange): void {
    this.onShowSmsChanged.next(event.checked);
  }
}
