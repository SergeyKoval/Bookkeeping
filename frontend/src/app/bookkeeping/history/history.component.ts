import {Component, OnInit} from '@angular/core';
import {Response} from '@angular/http';

import {Subscription} from 'rxjs/Subscription';
import {MdDialog} from '@angular/material';

import {HistoryService} from '../../common/service/history.service';
import {AuthenticationService} from '../../common/service/authentication.service';
import {ConfirmPopupService} from '../../common/components/confirm-popup/confirm-popup.service';
import {HistoryItem} from '../../common/model/history/HistoryItem';
import {AlertService} from '../../common/service/alert.service';
import {AlertType} from '../../common/model/alert/AlertType';
import {HistoryEditPopupComponent} from './history-edit-popup/history-edit-popup.component';
import {SettingsService} from '../../common/service/settings.service';

import 'rxjs/add/operator/switchMap';
import 'rxjs/add/operator/filter';
import 'rxjs/add/operator/do';
import 'rxjs/add/operator/first';

@Component({
  selector: 'bk-history',
  templateUrl: './history.component.html',
  styleUrls: ['./history.component.css']
})
export class HistoryComponent implements OnInit {
  public static readonly PAGE_LIMIT: number = 3;

  public loading: boolean = true;
  public loadingMoreIndicator: boolean = false;
  public disableMoreButton: boolean = false;

  public historyItems: HistoryType[];

  private authenticatedProfileId: number;


  public constructor(
    private _historyService: HistoryService,
    private _authenticationService: AuthenticationService,
    private _confirmPopupService: ConfirmPopupService,
    private _alertService: AlertService,
    private _dialog: MdDialog,
    private _settingsService: SettingsService
  ) {}

  public ngOnInit(): void {
    this.authenticatedProfileId = this._authenticationService.authenticatedProfile.id;
    const subscription: Subscription = this._historyService.loadHistoryItems(this.authenticatedProfileId, 1, HistoryComponent.PAGE_LIMIT)
      .subscribe((historyItems: HistoryType[]) => {
        if (historyItems.length < HistoryComponent.PAGE_LIMIT) {
          this.disableMoreButton = true;
        }
        this.historyItems = historyItems;
        subscription.unsubscribe();
        this.loading = false;
      });
  }

  public addHistoryItem(): void {

  }

  public editHistoryItem(historyItem: HistoryItem): void {
    this._dialog.open(HistoryEditPopupComponent, {
      width: '1000px',
      position: {top: 'top'},
      data: {
        'title': 'Редактирование операции',
        'historyItem': historyItem.originalItem
      },
    });
  }

  public loadMoreItems(numberOfNewItems: number): void {
    if (numberOfNewItems === HistoryComponent.PAGE_LIMIT) {
      this.showMoreHistoryItems();
    } else {
      this.reloadItems(numberOfNewItems);
    }
  }

  public deleteHistoryItem(historyItem: HistoryItem): void {
    const itemsLimit: number = this.historyItems.length;
    const subscription: Subscription = this._confirmPopupService.openConfirmPopup('Подтверждение', 'Точно удалить?')
      .afterClosed()
      .filter((result: boolean) => result === true)
      .do(() => this.loading = true)
      .switchMap(() => this.historyItems)
      .filter((historyType: HistoryType) => historyType.id === historyItem.id)
      .switchMap((historyType: HistoryType) => this._historyService.deleteHistoryItem(historyType))
      .do((response: Response) => {
        if (!response.ok) {
          this._alertService.addAlert(AlertType.WARNING, 'Возникла ошибка при удалении записи.');
          this.loading = false;
        } else {
          this._alertService.addAlert(AlertType.SUCCESS, 'Запись успешно удалена.');
        }
      })
      .filter((response: Response) => response.ok)
      .do(() => this._settingsService.loadAccounts(this.authenticatedProfileId))
      .switchMap(() => this._historyService.loadHistoryItems(this.authenticatedProfileId, 1, itemsLimit))
      .subscribe((historyItems: HistoryType[]) => {
        if (historyItems.length < itemsLimit) {
          this.disableMoreButton = true;
        }

        this.historyItems = historyItems;
        this.loading = false;
        subscription.unsubscribe();
      });
  }

  public getGoalDescription(goal: HistoryGoal): string {
    return goal.category ? `${goal.category} >> ${goal.name}` : goal.name;
  }

  private showMoreHistoryItems(): void {
    this.loadingMoreIndicator = true;
    const pageNumber: number = Math.ceil(this.historyItems.length / HistoryComponent.PAGE_LIMIT) + 1;

    const subscription: Subscription = this._historyService.loadHistoryItems(this.authenticatedProfileId, pageNumber, HistoryComponent.PAGE_LIMIT)
      .subscribe((historyItems: HistoryType[]) => {
        if (historyItems.length < HistoryComponent.PAGE_LIMIT) {
          this.disableMoreButton = true;
        }
        this.historyItems = this.historyItems.concat(historyItems);
        subscription.unsubscribe();
        this.loadingMoreIndicator = false;
      });
  }

  private reloadItems(numberOfNewItems: number): void {
    this.loading = true;
    const limit: number = this.historyItems.length + numberOfNewItems;
    const subscription: Subscription = this._historyService.loadHistoryItems(this.authenticatedProfileId, 1, limit)
      .subscribe((historyItems: HistoryType[]) => {
        if (historyItems.length < limit) {
          this.disableMoreButton = true;
        }
        this.historyItems = historyItems;
        subscription.unsubscribe();
        this.loading = false;
      });
  }
}
