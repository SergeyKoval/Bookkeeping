import { Component, OnInit } from '@angular/core';

import { ChartDataset, ChartOptions } from 'chart.js';

import { ProfileService } from '../../common/service/profile.service';
import { AlertService } from '../../common/service/alert.service';
import { AlertType } from '../../common/model/alert/AlertType';
import { ReportService } from '../../common/service/report.service';
import { CurrencyDetail } from '../../common/model/currency-detail';
import { DateUtils } from '../../common/utils/date-utils';
import { TendencyReport } from '../../common/model/report/tendency-report';

@Component({
  selector: 'bk-report-tendency',
  templateUrl: './report-tendency.component.html',
  styleUrls: ['./report-tendency.component.css'],
  standalone: false
})
export class ReportTendencyComponent implements OnInit {
  public loading: boolean = false;
  public defaultCurrency: CurrencyDetail;
  public startYear: number;
  public startMonth: number;
  public endYear: number;
  public endMonth: number;

  public lineChartData: ChartDataset[] = [];
  public lineChartLabels: string[] = [];
  public lineChartOptions: ChartOptions = {
    responsive: true,
    scales: {
      y: {
        grid: {
          color: (context) => context.tick.value === 0 ? 'rgba(0, 0, 0, 0.5)' : 'rgba(0, 0, 0, 0.1)',
          lineWidth: (context) => context.tick.value === 0 ? 2 : 1
        }
      }
    }
  };

  public reportData: TendencyReport[] = [];
  public totals = { income: 0, expense: 0, difference: 0 };

  public constructor(
    private _profileService: ProfileService,
    private _reportService: ReportService,
    private _alertService: AlertService
  ) {
    const now = new Date();
    this.endYear = now.getFullYear();
    this.endMonth = now.getMonth() + 1;
    this.startYear = this.endYear - 1;
    this.startMonth = this.endMonth;
  }

  public ngOnInit(): void {
    this.defaultCurrency = this._profileService.defaultCurrency;
  }

  public changeCurrency(currency: CurrencyDetail): void {
    this.defaultCurrency = currency;
  }

  public onStartYearChange(event: { year: number; month: number }): void {
    this.startYear = event.year;
    this.startMonth = event.month;
  }

  public onEndYearChange(event: { year: number; month: number }): void {
    this.endYear = event.year;
    this.endMonth = event.month;
  }

  public search(): void {
    if (!this.defaultCurrency) {
      this._alertService.addAlert(AlertType.WARNING, 'Валюта не выбрана');
      return;
    }

    if (this.startYear > this.endYear || (this.startYear === this.endYear && this.startMonth >= this.endMonth)) {
      this._alertService.addAlert(AlertType.WARNING, 'Начальная дата должна быть раньше конечной');
      return;
    }

    this.loading = true;
    this._reportService.getTendencyReport(
      this.defaultCurrency.name,
      this.startYear, this.startMonth,
      this.endYear, this.endMonth
    ).subscribe(items => {
      this.reportData = items;
      this.buildChart(items);
      this.calculateTotals(items);
      this.loading = false;
    });
  }

  private buildChart(items: TendencyReport[]): void {
    this.lineChartLabels = items.map(item =>
      `${DateUtils.MONTHS[item.month - 1]} ${item.year}`
    );

    const differences = items.map(item => item.difference);

    this.lineChartData = [{
      type: 'line',
      label: 'Разница между доходом и расходом',
      data: differences,
      fill: {
        target: 'origin',
        above: 'rgba(75, 192, 92, 0.3)',
        below: 'rgba(255, 99, 132, 0.3)'
      },
      borderColor: 'rgb(75, 132, 192)',
      pointBackgroundColor: differences.map(d => d >= 0 ? 'rgb(75, 192, 92)' : 'rgb(255, 99, 132)'),
      pointBorderColor: differences.map(d => d >= 0 ? 'rgb(75, 192, 92)' : 'rgb(255, 99, 132)'),
      pointRadius: 5,
      borderWidth: 2,
      tension: 0.3
    }];
  }

  private calculateTotals(items: TendencyReport[]): void {
    this.totals = items.reduce((acc, item) => ({
      income: acc.income + item.income,
      expense: acc.expense + item.expense,
      difference: acc.difference + item.difference
    }), { income: 0, expense: 0, difference: 0 });
  }

  public formatPeriod(month: number, year: number): string {
    return `${DateUtils.MONTHS[month - 1]} ${year}`;
  }
}
