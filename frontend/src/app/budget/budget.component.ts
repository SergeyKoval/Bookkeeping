import { Component, OnInit } from '@angular/core';

import { DateUtils } from '../common/utils/date-utils';
import { BudgetService } from '../common/service/budget.service';
import { DialogService } from '../common/service/dialog.service';
import { PlanBudgetDialogComponent } from './plan-budget-dialog/plan-budget-dialog.component';

@Component({
  selector: 'bk-budget',
  templateUrl: './budget.component.html',
  styleUrls: ['./budget.component.css']
})
export class BudgetComponent implements OnInit {
  public months: string[] = DateUtils.MONTHS;
  public loading: boolean;
  public selectedMonth: number;
  public selectedYear: number;
  public budget: Budget;
  public monthProgress: MonthProgress;

  public constructor(
    private _budgetService: BudgetService,
    private _dialogService: DialogService
  ) {}

  public ngOnInit(): void {
    const now: Date = new Date();
    this.selectedYear = now.getFullYear();
    this.selectedMonth = now.getMonth();
    this.loadBudget(true);
  }

  public getSelectedMonth(): string {
    return this.months[this.selectedMonth];
  }

  public chooseMonth(monthIndex: number): void {
    if (this.selectedMonth !== monthIndex) {
      this.selectedMonth = monthIndex;
      this.loadBudget(true);
    }
  }

  public decreaseYear(): void {
    if (!this.loading) {
      this.selectedYear--;
      this.selectedMonth = 11;
      this.loadBudget(true);
    }
  }

  public increaseYear(): void {
    if (!this.loading) {
      this.selectedYear++;
      this.selectedMonth = 0;
      this.loadBudget(true);
    }
  }

  public openPlanDialog(): void {
    if (!this.loading) {
      this._dialogService.openDialog(PlanBudgetDialogComponent, {
        panelClass: 'budget-plan-dialog',
        width: '400px',
        position: {top: 'top'},
        data: {
          'editMode': false,
          'type': 'goal',
          'budget': this.budget
        }
      }).afterClosed()
        .subscribe(refreshBudget => {
          if (refreshBudget === true) {
            this.loadBudget(true);
          }
        });
    }
  }

  public loadBudget(refresh: boolean): void {
    if (refresh === true) {
      this.loading = true;
      this.updateMonthProgress();
      this._budgetService.loadBudget(this.selectedYear, this.selectedMonth + 1).subscribe((budget: Budget) => {
        if (this.budget && this.budget.year === budget.year && this.budget.month === budget.month) {
          this.setOpenedCategories('income', budget);
          this.setOpenedCategories('expense', budget);
        }
        this.budget = budget;
        this.loading = false;
      });
    }
  }

  private updateMonthProgress(): void {
    const now: Date = new Date();
    const currentMonth: boolean = now.getFullYear() === this.selectedYear && now.getMonth() === this.selectedMonth;
    const monthPercent: number = currentMonth ? Math.round(now.getDate() / DateUtils.daysInMonth(this.selectedYear, this.selectedMonth + 1) * 100) : 0;
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
