import { Component, OnInit } from '@angular/core';

import { IMyDate } from 'angular-mydatepicker';
import { ChartDataset } from 'chart.js';

import { MultiLevelDropdownItem } from '../../common/components/multi-level-dropdown/MultiLevelDropdownItem';
import { ProfileService } from '../../common/service/profile.service';
import { AssetImagePipe } from '../../common/pipes/asset-image.pipe';
import { BaseReport } from '../BaseReport';
import { CheckboxState } from '../../common/components/three-state-checkbox/CheckboxState';
import { AlertType } from '../../common/model/alert/AlertType';
import { AlertService } from '../../common/service/alert.service';
import { DateUtils } from '../../common/utils/date-utils';
import { ReportService } from '../../common/service/report.service';
import { CurrencyDetail } from '../../common/model/currency-detail';
import { Profile } from '../../common/model/profile';
import { DynamicReport } from '../../common/model/report/dynamic-report';

@Component({
  selector: 'bk-report-dynamic',
  templateUrl: './report-dynamic.component.html',
  styleUrls: ['./report-dynamic.component.css']
})
export class ReportDynamicComponent extends BaseReport implements OnInit {
  public loading: boolean;
  public operationsFilter: MultiLevelDropdownItem[];
  public defaultCurrency: CurrencyDetail;
  public lineChartData: ChartDataset[];
  public lineChartLabels: string[];

  public constructor(
    protected _profileService: ProfileService,
    protected _imagePipe: AssetImagePipe,
    private _reportService: ReportService,
    private _alertService: AlertService
  ) {
    super(_profileService, _imagePipe);
    this.datePickerOptions.dateFormat = 'mm.yyyy';
    this.datePickerOptions.selectorWidth = '190px';
    // this.datePickerOptions.width = '190px';
  }

  public ngOnInit(): void {
    const profile: Profile = this._profileService.authenticatedProfile;
    this.defaultCurrency = this._profileService.defaultCurrency;
    this.operationsFilter = this.populateCategoriesFilter(profile.categories);
  }

  public changeCurrency(currency: CurrencyDetail): void {
    this.defaultCurrency = currency;
  }

  public search(): void {
    if (!this.periodFilter) {
      this._alertService.addAlert(AlertType.WARNING, 'Период не выбран');
      return;
    }

    const numberOfPeriodsSelected: number = this.getNumberOfPeriodsSelected();
    if (numberOfPeriodsSelected < 1 || numberOfPeriodsSelected > 8) {
      this._alertService.addAlert(AlertType.WARNING, 'Выберите период от 1 до 8 месцев');
      return;
    }

    if (this.operationsFilter.filter(operation => operation.state !== CheckboxState.UNCHECKED).length === 0) {
      this._alertService.addAlert(AlertType.WARNING, 'Фильтр операций пуст');
      return;
    }

    this.loading = true;
    this.lineChartLabels = [];
    this.lineChartData = [];
    this._reportService.getDynamicForPeriodReport(this.defaultCurrency.name, this.periodFilter, this.operationsFilter)
      .subscribe(items => {
        const periods: Map<number, Set<number>> = new Map();
        const labels: Set<string> = new Set<string>();

        items.forEach(item => {
          if (!periods.has(item.year)) {
            periods.set(item.year, new Set<number>());
          }
          periods.get(item.year).add(item.month);

          labels.add(item.category + (item.subCategory ? ` >> ${item.subCategory}` : ''));
        });
        labels.forEach(value => this.lineChartData.push({label: value, fill: false, data: []}));

        Array.from(periods.keys()).sort().forEach(year => {
          Array.from(periods.get(year)).sort().forEach(month => {
            this.lineChartLabels.push(`${DateUtils.MONTHS[month - 1]} ${year}`);
            const periodItems: DynamicReport[] = items.filter(item => item.month === month && item.year === year);
            this.lineChartData.forEach(dataItem => {
              const suitableItem: DynamicReport = periodItems.filter(item => dataItem.label === item.category + (item.subCategory ? ` >> ${item.subCategory}` : ''))[0];
              dataItem.data.push(suitableItem ? suitableItem.value : 0);
            });
          });
        });

        this.loading = false;
      });
  }

  private getNumberOfPeriodsSelected(): number {
    const endDate: IMyDate = this.periodFilter.endDate;
    const beginDate: IMyDate = this.periodFilter.beginDate;
    if (endDate.year === beginDate.year) {
      return endDate.month - beginDate.month + 1;
    } else if (endDate.year - beginDate.year === 1) {
      return endDate.month + 12 - beginDate.month + 1;
    } else {
      return endDate.month + 12 - beginDate.month + 1 + 12 * (endDate.year - beginDate.year - 1);
    }
  }
}
