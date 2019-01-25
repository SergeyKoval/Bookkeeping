import { Component, OnInit } from '@angular/core';

import { IMyDrpOptions } from 'mydaterangepicker';

import { DateUtils } from '../../common/utils/date-utils';

@Component({
  selector: 'bk-report-actions',
  templateUrl: './report-actions.component.html',
  styleUrls: ['./report-actions.component.css']
})
export class ReportActionsComponent implements OnInit {
  public loading: boolean;
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

  public constructor() { }

  public ngOnInit(): void {
  }

  public onDateRangeChanged(event: any): void {
    console.log(event);
  }
}
