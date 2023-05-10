import { Component, OnInit } from '@angular/core';
import { MatDialog, MatDialogRef } from '@angular/material/dialog';

import { of } from 'rxjs';
import { filter, switchMap, tap } from 'rxjs/operators';

import { MultiLevelDropdownItem } from '../../common/components/multi-level-dropdown/MultiLevelDropdownItem';
import { CheckboxState } from '../../common/components/three-state-checkbox/CheckboxState';
import { ProfileService } from '../../common/service/profile.service';
import { AssetImagePipe } from '../../common/pipes/asset-image.pipe';
import { AlertService } from '../../common/service/alert.service';
import { AlertType } from '../../common/model/alert/AlertType';
import { ReportService } from '../../common/service/report.service';
import { BaseReport } from '../BaseReport';
import { HistoryItem } from '../../common/model/history/HistoryItem';
import { HistoryEditDialogComponent } from '../../history/history-edit-dialog/history-edit-dialog.component';
import { LoadingDialogComponent } from '../../common/components/loading-dialog/loading-dialog.component';
import { CurrencyUtils } from '../../common/utils/currency-utils';
import { ConfirmDialogService } from '../../common/components/confirm-dialog/confirm-dialog.service';
import { LoadingService } from '../../common/service/loading.service';
import { BudgetService } from '../../common/service/budget.service';
import { HistoryService } from '../../common/service/history.service';

@Component({
  selector: 'bk-report-actions',
  templateUrl: './report-actions.component.html',
  styleUrls: ['./report-actions.component.css']
})
export class ReportActionsComponent extends BaseReport implements OnInit {
  public loading: boolean;
  public operationsFilter: MultiLevelDropdownItem[];
  public accountsFilter: MultiLevelDropdownItem[];
  public historyItems: HistoryType[];

  public constructor(
    protected _profileService: ProfileService,
    protected _imagePipe: AssetImagePipe,
    private _alertService: AlertService,
    private _dialog: MatDialog,
    private _confirmDialogService: ConfirmDialogService,
    private _loadingService: LoadingService,
    private _budgetService: BudgetService,
    private _historyService: HistoryService,
    private _authenticationService: ProfileService,
    private _reportService: ReportService
  ) {
    super(_profileService, _imagePipe);
  }

  public ngOnInit(): void {
    const profile: Profile = this._profileService.authenticatedProfile;
    this.operationsFilter = this.populateCategoriesFilter(profile.categories);
    this.accountsFilter = this.populateAccountsFilter(profile.accounts);
  }

  public search(): void {
    if (!this.periodFilter) {
      this._alertService.addAlert(AlertType.WARNING, 'Период не выбран');
      return;
    }

    if (this.operationsFilter.filter(operation => operation.state !== CheckboxState.UNCHECKED).length === 0) {
      this._alertService.addAlert(AlertType.WARNING, 'Фильтр операций пуст');
      return;
    }

    if (this.accountsFilter.filter(account => account.state !== CheckboxState.UNCHECKED).length === 0) {
      this._alertService.addAlert(AlertType.WARNING, 'Фильтр счетов пуст');
      return;
    }

    this.loading = true;
    this._reportService.getHistoryItemsForPeriodReport(this.periodFilter, this.operationsFilter, this.accountsFilter).subscribe((items: HistoryType[]) => {
      this.historyItems = items;
      this.loading = false;
    });
  }

  public editHistoryItem(historyItem: HistoryItem): void {
    this._dialog.open(HistoryEditDialogComponent, {
      width: '720px',
      position: {top: 'top'},
      panelClass: 'history-add-edit-dialog',
      data: {
        'historyItem': historyItem.cloneOriginalItem(),
        'editMode': true
      }
    }).afterClosed()
      .pipe(filter((result: boolean) => result === true))
      .subscribe(() => this.search());
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
            this.search();
          }
        }),
        filter(simpleResponse => simpleResponse.status === 'SUCCESS')
      ).subscribe(() => {
      this._alertService.addAlert(AlertType.SUCCESS, 'Запись успешно удалена');
      this._authenticationService.quiteReloadAccounts();
      this.search();
    });
  }

  protected populateCategoriesFilter(categories: Category[]): MultiLevelDropdownItem[] {
    const categoriesFilter: MultiLevelDropdownItem[] = super.populateCategoriesFilter(categories);
    categoriesFilter.push(new MultiLevelDropdownItem('Перевод', CheckboxState.CHECKED, null, null, 'transfer'));
    categoriesFilter.push(new MultiLevelDropdownItem('Обмен', CheckboxState.CHECKED, null, null, 'exchange'));
    categoriesFilter.push(new MultiLevelDropdownItem('Изм. ост.', CheckboxState.CHECKED, null, null, 'balance'));

    return categoriesFilter;
  }
}
