import {Component, OnInit} from '@angular/core';

import {Subscription} from 'rxjs/Subscription';

import {HistoryService} from '../../common/service/history.service';
import {AuthenticationService} from '../../common/service/authentication.service';

@Component({
  selector: 'bk-history',
  templateUrl: './history.component.html',
  styleUrls: ['./history.component.css']
})
export class HistoryComponent implements OnInit {
  private static readonly _PAGE_LIMIT: number = 3;

  public loading: boolean = true;
  public loadingMoreIndicator: boolean = false;
  public disableMoreButton: boolean = false;

  public historyItems: HistoryType[];

  private authenticatedProfileId: number;


  public constructor(
    private _historyService: HistoryService,
    private _authenticationService: AuthenticationService
  ) {}

  public ngOnInit(): void {
    this.authenticatedProfileId = this._authenticationService.authenticatedProfile.id;
    const subscription: Subscription = this._historyService.loadHistoryItems(this.authenticatedProfileId, 1, HistoryComponent._PAGE_LIMIT)
      .subscribe((historyItems: HistoryType[]) => {
        this.historyItems = historyItems;
        subscription.unsubscribe();
        this.loading = false;
      });
  }

  public addHistoryItem(): void {

  }

  public showMoreHistoryItems(): void {
    this.loadingMoreIndicator = true;
    const pageNumber: number = this.historyItems.length / HistoryComponent._PAGE_LIMIT + 1;

    const subscription: Subscription = this._historyService.loadHistoryItems(this.authenticatedProfileId, pageNumber, HistoryComponent._PAGE_LIMIT)
      .subscribe((historyItems: HistoryType[]) => {
        if (historyItems.length < HistoryComponent._PAGE_LIMIT) {
          this.disableMoreButton = true;
        }
        this.historyItems = this.historyItems.concat(historyItems);
        subscription.unsubscribe();
        this.loadingMoreIndicator = false;
      });
  }

  public getGoalDescription(goal: HistoryGoal): string {
    return goal.category ? `${goal.category} >> ${goal.name}` : goal.name;
  }
}
