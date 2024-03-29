import { Component, OnInit } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';

import { filter } from 'rxjs/operators';

import { DateUtils } from '../common/utils/date-utils';
import { BudgetService } from '../common/service/budget.service';
import { PlanBudgetDialogComponent } from './plan-budget-dialog/plan-budget-dialog.component';
import { CloseMonthDialogComponent } from './close-month-dialog/close-month-dialog.component';
import { LoadingService } from '../common/service/loading.service';
import { Budget } from '../common/model/budget/budget';
import { MonthProgress } from '../common/model/budget/month-progress';
import { BudgetCategory } from '../common/model/budget/budget-category';

@Component({
  selector: 'bk-budget',
  templateUrl: './budget.component.html',
  styleUrls: ['./budget.component.css']
})
export class BudgetComponent implements OnInit {
  public loading: boolean;
  public budget: Budget;
  public monthProgress: MonthProgress;

  public constructor(
    private _budgetService: BudgetService,
    private _dialog: MatDialog,
    private _loadingService: LoadingService
  ) {}

  public ngOnInit(): void {
    const now: Date = new Date();
    this.loadBudget(true, now.getFullYear(), now.getMonth() + 1);
  }

  public chooseMonth(monthIndex: number): void {
    this.loadBudget(true, this.budget.year, monthIndex);
  }

  public chooseYear(date: {year: number, month: number}): void {
    this.loadBudget(true, date.year, date.month);
  }

  public openPlanDialog(): void {
    if (!this.loading) {
      this._dialog.open(PlanBudgetDialogComponent, {
        panelClass: 'budget-plan-dialog',
        width: '400px',
        position: {top: 'top'},
        data: {
          'editMode': false,
          'type': 'goal',
          'budget': this.budget,
          'postpone': false
        }
      }).afterClosed()
        .subscribe(refreshBudget => {
          if (refreshBudget === true) {
            this.loadBudget(true, this.budget.year, this.budget.month);
          }
        });
    }
  }

  public openCloseMonthDialog(): void {
    if (!this.loading && !this.isFuturePeriod()) {
      this._dialog.open(CloseMonthDialogComponent, {
        panelClass: 'budget-close-month-dialog',
        width: '650px',
        position: {top: 'top'},
        data: {
          'budget': this.budget
        }
      }).afterClosed()
        .pipe(filter(loadNextPeriodBudget => loadNextPeriodBudget))
        .subscribe((nextMonthPeriod: {year: number, month: number}) => this.loadBudget(true, nextMonthPeriod.year, nextMonthPeriod.month));
    }
  }

  public loadBudget(refresh: boolean, year: number = this.budget.year, month: number = this.budget.month): void {
    if (refresh === true) {
      this.loading = true;
      this._budgetService.loadBudget(year, month).subscribe((budget: Budget) => {
        if (this.budget && this.budget.year === budget.year && this.budget.month === budget.month) {
          this.setOpenedCategories('income', budget);
          this.setOpenedCategories('expense', budget);
        }
        this.budget = budget;
        this.updateMonthProgress();
        this.loading = false;
      });
    }
  }

  public isFuturePeriod(): boolean {
    const today: Date = new Date();
    return this.budget.year > today.getFullYear() || this.budget.year === today.getFullYear() && this.budget.month > today.getMonth() + 1;
  }

  private updateMonthProgress(): void {
    const now: Date = new Date();
    const currentMonth: boolean = now.getFullYear() === this.budget.year && now.getMonth() === this.budget.month - 1;
    const monthPercent: number = currentMonth ? Math.round(now.getDate() / DateUtils.daysInMonth(this.budget.year, this.budget.month) * 100) : 0;
    this.monthProgress = {'currentMonth': currentMonth, 'monthPercent': monthPercent};
  }

  private setOpenedCategories(type: string, budget: Budget): void {
    this.budget[type].categories
      .filter(category => category.opened)
      .forEach(category => {
        const budgetCategory: BudgetCategory = budget[type].categories.filter(updatedCategory => updatedCategory.title === category.title)[0];
        if (budgetCategory) {
          budgetCategory.opened = category.opened;
        }
      });
  }
}
