import { Component, OnInit } from '@angular/core';

import { IMyDateRangeModel, IMyDrpOptions } from 'mydaterangepicker';
import { from } from 'rxjs';
import { groupBy, mergeMap, tap, toArray } from 'rxjs/operators';

import { MultiLevelDropdownItem } from '../../common/components/multi-level-dropdown/MultiLevelDropdownItem';
import { DateUtils } from '../../common/utils/date-utils';
import { ProfileService } from '../../common/service/profile.service';
import { CheckboxState } from '../../common/components/three-state-checkbox/CheckboxState';
import { BaseReport } from '../BaseReport';
import { AssetImagePipe } from '../../common/pipes/asset-image.pipe';
import { CurrencyUtils } from '../../common/utils/currency-utils';
import { AlertType } from '../../common/model/alert/AlertType';
import { AlertService } from '../../common/service/alert.service';
import { ReportService } from '../../common/service/report.service';
import { CurrencyService } from '../../common/service/currency.service';

@Component({
  selector: 'bk-report-summary',
  templateUrl: './report-summary.component.html',
  styleUrls: ['./report-summary.component.css']
})
export class ReportSummaryComponent extends BaseReport implements OnInit {
  public pieChartType: string = 'pie';
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
  public currenciesFilter: MultiLevelDropdownItem[] = [];
  public periodFilter: IMyDateRangeModel;
  public items: SummaryReport[][] = [];
  public pieChartLabels: string[];
  public pieChartData: number[];
  public type: string;
  public onlyCategories: boolean = true;

  private reportSum: number = 0;
  private reportCurrency: string;

  public constructor(
    protected _profileService: ProfileService,
    protected _imagePipe: AssetImagePipe,
    private _alertService: AlertService,
    private _reportService: ReportService,
    private _currencyService: CurrencyService
  ) {
    super(_profileService, _imagePipe);
  }

  public ngOnInit(): void {
    const profile: Profile = this._profileService.authenticatedProfile;
    this.operationsFilter = this.populateCategoriesFilter(profile.categories);
    this.accountsFilter = this.populateAccountsFilter(profile.accounts);
    profile.currencies.forEach(currency => {
      this.currenciesFilter.push(new MultiLevelDropdownItem(CurrencyUtils.convertCodeToSymbol(currency.symbol), CheckboxState.CHECKED, null, null, currency.name));
    });
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

    if (this.currenciesFilter.filter(currency => currency.state !== CheckboxState.UNCHECKED).length === 0) {
      this._alertService.addAlert(AlertType.WARNING, 'Фильтр валют пуст');
      return;
    }

    this.loading = true;
    this.pieChartLabels = [];
    this.pieChartData = [];
    this.reportSum = 0;
    this.items = [];
    this.type = this.operationsFilter.filter(operation => operation.state === CheckboxState.CHECKED)[0].alias;
    this.onlyCategories = true;

    let defaultCurrency: string = this._profileService.defaultCurrency.name;
    if (this.currenciesFilter.filter(currency => currency.alias === defaultCurrency).length === 0) {
      defaultCurrency = this.currenciesFilter[0].alias;
    }
    this.reportCurrency = defaultCurrency;

    this._reportService.getSummaryForPeriodReport(this.periodFilter, this.operationsFilter, this.accountsFilter, this.currenciesFilter)
      .pipe(tap(items => {
        items.map(item => item.values).forEach((valueMap: {[currency: string]: number}) => {
          Object.keys(valueMap)
            .forEach(currency => this.reportSum = this.reportSum + this._currencyService.convertToCurrency(valueMap[currency], currency, this.reportCurrency));
        });

        items.forEach(item => {
          if (this.onlyCategories && item.subCategory) {
            this.onlyCategories = false;
          }

          this.pieChartLabels.push(item.subCategory ? `${item.category} >> ${item.subCategory}` : item.category);
          let itemValue: number = 0;
          Object.keys(item.values)
            .forEach(currency => itemValue = itemValue + this._currencyService.convertToCurrency(item.values[currency], currency, this.reportCurrency));
          const percent: number = 100 * itemValue / this.reportSum;
          item.percent = Number(percent.toFixed(2));
          this.pieChartData.push(item.percent);
        });
      })).subscribe((items: SummaryReport[]) => {
        from(items).pipe(
          groupBy(item => item.category),
          mergeMap(group => group.pipe(toArray()))
        ).subscribe(categoryGroup => this.items.push(categoryGroup));
        this.loading = false;
    });
  }
}
