import { Component, OnInit } from '@angular/core';

import { IMyDateRangeModel, IMyDrpOptions } from 'mydaterangepicker';

import { MultiLevelDropdownItem } from '../../common/components/multi-level-dropdown/MultiLevelDropdownItem';
import { DateUtils } from '../../common/utils/date-utils';

@Component({
  selector: 'bk-report-summary',
  templateUrl: './report-summary.component.html',
  styleUrls: ['./report-summary.component.css']
})
export class ReportSummaryComponent implements OnInit {
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
  public operationsFilter: MultiLevelDropdownItem[] = [];
  public accountsFilter: MultiLevelDropdownItem[] = [];
  public periodFilter: IMyDateRangeModel;
  // Pie
  public pieChartLabels: string[] = ['Download Sales', 'In-Store Sales', 'Mail Sales'];
  public pieChartData: number[] = [300, 500, 100];
  public pieChartType: string = 'pie';

  constructor() { }

  ngOnInit() {
  }

  // events
  public chartClicked(e:any): void {
    console.log(e);
  }

  public chartHovered(e:any): void {
    console.log(e);
  }

  public onDateRangeChanged(dateRange: IMyDateRangeModel): void {
    this.periodFilter = dateRange;
  }

  public search(): void {

  }

}
