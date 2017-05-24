import { Component, OnInit } from '@angular/core';

import {Subscription} from 'rxjs/Subscription';

import {HistoryService} from '../../common/service/history.service';
import {AuthenticationService} from '../../common/service/authentication.service';

@Component({
  selector: 'bk-history',
  templateUrl: './history.component.html',
  styleUrls: ['./history.component.css']
})
export class HistoryComponent implements OnInit {
  private static readonly _PAGE_LIMIT: number = 2;

  public loading: boolean = true;
  public loadingMoreIndicator: boolean = false;

  public historyItems: HistoryType[];


  public constructor(
    private _historyService: HistoryService,
    private _authenticationService: AuthenticationService
  ) {}

  public ngOnInit(): void {
    const subscription: Subscription = this._historyService.loadHistoryItems(this._authenticationService.authenticatedProfile.id, 1, HistoryComponent._PAGE_LIMIT)
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

  }

  public getGoalDescription(goal: HistoryGoal): string {
    return goal.category ? `${goal.category} >> ${goal.name}` : goal.name;
  }
}
