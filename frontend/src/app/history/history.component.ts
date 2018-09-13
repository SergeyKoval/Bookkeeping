import { Component, OnInit } from '@angular/core';
import { MatDialogRef } from '@angular/material';

import { filter, switchMap, tap } from 'rxjs/operators';

import { HistoryService } from '../common/service/history.service';
import { ProfileService } from '../common/service/profile.service';
import { ConfirmDialogService } from '../common/components/confirm-dialog/confirm-dialog.service';
import { HistoryItem } from '../common/model/history/HistoryItem';
import { AlertService } from '../common/service/alert.service';
import { AlertType } from '../common/model/alert/AlertType';
import { HistoryEditDialogComponent } from './history-edit-dialog/history-edit-dialog.component';
import { DialogService } from '../common/service/dialog.service';
import { BudgetService } from '../common/service/budget.service';
import { Observable, of } from 'rxjs';
import { CurrencyUtils } from '../common/utils/currency-utils';
import { LoadingService } from '../common/service/loading.service';
import { LoadingDialogComponent } from '../common/components/loading-dialog/loading-dialog.component';

@Component({
  selector: 'bk-history',
  templateUrl: './history.component.html',
  styleUrls: ['./history.component.css']
})
export class HistoryComponent implements OnInit {
  public static readonly PAGE_LIMIT: number = 10;

  public loading: boolean = true;
  public loadingMoreIndicator: boolean = false;
  public disableMoreButton: boolean = false;

  public historyItems: HistoryType[] = [];

  public constructor(
    private _dialogService: DialogService,
    private _historyService: HistoryService,
    private _authenticationService: ProfileService,
    private _confirmDialogService: ConfirmDialogService,
    private _alertService: AlertService,
    private _budgetService: BudgetService,
    private _profileService: ProfileService,
    private _loadingService: LoadingService
  ) {}

  public ngOnInit(): void {
    this.init(1, HistoryComponent.PAGE_LIMIT);
  }

  public loadMoreItems(numberOfNewItems: number): void {
    this.init(1, this.historyItems.length + numberOfNewItems);
  }

  public editHistoryItem(historyItem: HistoryItem): void {
    this._dialogService.openDialog(HistoryEditDialogComponent, {
      width: '720px',
      position: {top: 'top'},
      panelClass: 'history-add-edit-dialog',
      data: {
        'historyItem': historyItem.cloneOriginalItem(),
        'editMode': true
      }
    }).afterClosed()
      .pipe(filter((result: boolean) => result === true))
      .subscribe(() => this.loadMoreItems(0));
  }

  public deleteHistoryItem(historyItem: HistoryItem): void {
    let loadingDialog: MatDialogRef<LoadingDialogComponent>;
    this._confirmDialogService.openConfirmDialog('Подтверждение', 'Точно удалить?')
      .afterClosed()
      .pipe(
        filter((result: boolean) => result === true),
        tap(() => loadingDialog = this._loadingService.openLoadingDialog('Проверка цели...')),
        switchMap(() => historyItem.goal ? this._budgetService.reviewBudgetGoalBeforeDelete(historyItem.originalItem.id, historyItem.originalItem.goal) : of({})),
        switchMap((simpleResponse: SimpleResponse) => {
          loadingDialog.close();
          if (simpleResponse.status === 'FAIL') {
            const result: {} = simpleResponse.result;
            return this._confirmDialogService.openConfirmDialogWithHtml('Изменение статуса цели',
              /* tslint:disable:max-line-length */
    `<div>Удаляемая операция является частью выполненной цели.</div>
              <div>После ее удаления прогресс цели будет <strong>${CurrencyUtils.convertCodeToSymbol(this._profileService.getCurrencyDetails(result['currency']).symbol)} ${result['value']} / ${result['completeValue']}.</strong></div>
              <div>Пометить цель невыполненной?</div>`).afterClosed();
              /* tslint:enable */
          } else {
            return of(false);
          }
        }),
        tap(result => loadingDialog = this._loadingService.openLoadingDialog('Удаление...')),
        switchMap(changeGoalStatus => this._historyService.deleteHistoryItem(historyItem.originalItem.id, changeGoalStatus)),
        tap(simpleResponse => {
          loadingDialog.close();
          if (simpleResponse.status === 'FAIL') {
            this._alertService.addAlert(AlertType.WARNING, 'Ошибка при удалении');
            this.loadMoreItems(0);
          }
        }),
        filter(simpleResponse => simpleResponse.status === 'SUCCESS')
      ).subscribe(() => {
        this._alertService.addAlert(AlertType.SUCCESS, 'Запись успешно удалена');
        this._authenticationService.quiteReloadAccounts();
        this.loadMoreItems(-1);
      });
  }

  private init(page: number, limit: number): void {
    this.loading = true;
    this._historyService.loadHistoryItems(page, limit).subscribe((historyItems: HistoryType[]) => {
      if (historyItems.length < limit) {
        this.disableMoreButton = true;
      }
      this.historyItems = historyItems;
      this.loading = false;
    });
  }
}
