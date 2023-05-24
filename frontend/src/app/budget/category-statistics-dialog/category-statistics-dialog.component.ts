import { Component, Inject, OnInit } from '@angular/core';

import { BudgetService } from '../../common/service/budget.service';
import { DateUtils } from '../../common/utils/date-utils';
import { CurrencyService } from '../../common/service/currency.service';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { MatSlideToggleChange } from '@angular/material/slide-toggle';
import { BudgetStatistic } from '../../common/model/budget/budget-statistic';
import { BudgetBalance } from '../../common/model/budget/budget-balance';
import { CurrencyDetail } from '../../common/model/currency-detail';

@Component({
  selector: 'bk-category-statistics-dialog',
  templateUrl: './category-statistics-dialog.component.html',
  styleUrls: ['./category-statistics-dialog.component.css']
})
export class CategoryStatisticsDialogComponent implements OnInit {
  public months: string[] = DateUtils.MONTHS;
  public loading: boolean = true;
  public categoryStatistics: BudgetStatistic[];
  public currency: string;
  public currencyBalance: BudgetBalance[];

  private statistics: BudgetStatistic[];
  private maxCurrencyValues: Map<string, number>;

  public constructor(
    @Inject(MAT_DIALOG_DATA) public data: {budgetType: string, category: string, year: number, month: number},
    private _dialogRef: MatDialogRef<CategoryStatisticsDialogComponent>,
    private _budgetService: BudgetService,
    private _currencyService: CurrencyService
  ) {}

  public ngOnInit(): void {
    this._budgetService.categoryStatistics(this.data).subscribe(categoryStatistics => {
      categoryStatistics.forEach(categoryStatistic => categoryStatistic.used = true);
      this.statistics = this.categoryStatistics = categoryStatistics;
      this.calculateMaxAndAverageValues();
      this.loading = false;
    });
  }

  public changeCurrency(currencyDetail: CurrencyDetail): void {
    this.currency = currencyDetail ? currencyDetail.name : null;
    this.categoryStatistics = this.convertToCurrency();
    this.calculateMaxAndAverageValues();
  }

  public getMonthTitle(monthNumber: number): string {
    return this.months[monthNumber - 1];
  }

  public getPercent(currency: string, value: number): number {
    return value / this.maxCurrencyValues.get(currency) * 100;
  }

  public save(): void {
    this._dialogRef.close(this.currencyBalance.filter(balance => balance.selectedValue));
  }

  public changeSelectedValue(budgetBalance: BudgetBalance): void {
    budgetBalance.selectedValue = !budgetBalance.selectedValue;
  }

  public close(): void {
    this._dialogRef.close([]);
  }

  public recalculateStatistics (event: MatSlideToggleChange, categoryStatistic: BudgetStatistic): void {
    categoryStatistic.used = event.checked;
    this.calculateMaxAndAverageValues();
  }

  private calculateMaxAndAverageValues(): void {
    const averageValues: Map<string, number> = new Map<string, number>();
    this.maxCurrencyValues = new Map<string, number>();
    const usedCategoryStatistics: BudgetStatistic[] = this.categoryStatistics.filter(categoryStatistic => categoryStatistic.used);
    usedCategoryStatistics.map(categoryStatistic => categoryStatistic.category.balance)
      .forEach(balance => {
        Object.keys(balance).forEach(currency => {
          if (balance[currency].value > (this.maxCurrencyValues.get(currency) || 0)) {
            this.maxCurrencyValues.set(currency, balance[currency].value);
          }

          averageValues.set(currency, balance[currency].value + (averageValues.get(currency) | 0));
        });
      });

    this.currencyBalance = [];
    averageValues.forEach((value, currency) => {
      const completeValue: number = Number((value / usedCategoryStatistics.length).toFixed(2));
      this.currencyBalance.push({'currency': currency, 'completeValue': completeValue, 'selectedValue': true});
    });
  }

  private convertToCurrency(): BudgetStatistic[] {
    if (!this.currency) {
      return this.statistics;
    }

    const result: BudgetStatistic[] = [];
    this.statistics.forEach(categoryStatistic => {
      const balance: BudgetBalance = categoryStatistic.category.balance;
      let currencyValue: number = 0;
      Object.keys(balance).forEach(balanceCurrency => {
        if (balanceCurrency === this.currency) {
          currencyValue += balance[balanceCurrency].value;
        } else {
          currencyValue += this._currencyService.convertToCurrency(balance[balanceCurrency].value, balanceCurrency, this.currency);
        }
      });

      const item: BudgetStatistic = Object.assign({}, categoryStatistic);
      item.category = Object.assign({}, item.category);
      item.category.balance = {};
      item.category.balance[this.currency] = {value: currencyValue};
      result.push(item);
    });

    return result;
  }
}
