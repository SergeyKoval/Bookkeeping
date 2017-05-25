import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';

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

  public constructor() { }

  public ngOnInit(): void {
  }

  public addHistoryItem(): void {

  }

  public showMoreHistoryItems(): void {
    this.loadMore.emit();
  }

  public moreButtonDisabledValue(): string {
    return this.disableMoreButton ? 'true' : null;
  }
}
