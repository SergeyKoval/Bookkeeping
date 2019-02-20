import { Component, OnInit } from '@angular/core';

import { IMyDateRangeModel, IMyDrpOptions } from 'mydaterangepicker';

import { DateUtils } from '../../common/utils/date-utils';
import { MultiLevelDropdownItem } from '../../common/components/multi-level-dropdown/MultiLevelDropdownItem';
import { CheckboxState } from '../../common/components/three-state-checkbox/CheckboxState';
import { ProfileService } from '../../common/service/profile.service';
import { AssetImagePipe } from '../../common/pipes/asset-image.pipe';
import { AlertService } from '../../common/service/alert.service';
import { AlertType } from '../../common/model/alert/AlertType';
import { ReportService } from '../../common/service/report.service';
import { BaseReport } from '../BaseReport';

@Component({
  selector: 'bk-report-actions',
  templateUrl: './report-actions.component.html',
  styleUrls: ['./report-actions.component.css']
})
export class ReportActionsComponent extends BaseReport implements OnInit {
  public datePickerOptions: IMyDrpOptions = {
    dateFormat: 'dd.mm.yyyy',
    inline: false,
    dayLabels: DateUtils.DAY_LABELS,
    monthLabels: DateUtils.MONTH_LABELS,
    selectBeginDateTxt: 'Выберите начало периода',
    selectEndDateTxt: 'Выберите конец периода',
    width: '225px',
    height: '32px',
    selectorWidth: '225px',
    disableSince: {year: new Date().getFullYear(), month: new Date().getMonth() + 1, day: new Date().getDate() + 1}
  };
  public loading: boolean;
  public operationsFilter: MultiLevelDropdownItem[];
  public accountsFilter: MultiLevelDropdownItem[];
  public periodFilter: IMyDateRangeModel;
  public historyItems: HistoryType[];

  public constructor(
    protected _profileService: ProfileService,
    protected _imagePipe: AssetImagePipe,
    private _alertService: AlertService,
    private _reportService: ReportService
  ) {
    super(_profileService, _imagePipe);
  }

  public ngOnInit(): void {
    const profile: Profile = this._profileService.authenticatedProfile;
    this.operationsFilter = this.populateCategoriesFilter(profile.categories);
    this.accountsFilter = this.populateAccountsFilter(profile.accounts);
  }

  public onDateRangeChanged(dateRange: IMyDateRangeModel): void {
    this.periodFilter = dateRange;
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

  protected populateCategoriesFilter(categories: Category[]): MultiLevelDropdownItem[] {
    const categoriesFilter: MultiLevelDropdownItem[] = super.populateCategoriesFilter(categories)
    categoriesFilter.push(new MultiLevelDropdownItem('Перевод', CheckboxState.CHECKED, null, null, 'transfer'));
    categoriesFilter.push(new MultiLevelDropdownItem('Обмен', CheckboxState.CHECKED, null, null, 'exchange'));
    categoriesFilter.push(new MultiLevelDropdownItem('Изм. ост.', CheckboxState.CHECKED, null, null, 'balance'));

    return categoriesFilter;
  }
}
