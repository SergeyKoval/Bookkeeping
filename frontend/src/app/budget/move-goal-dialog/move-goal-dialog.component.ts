import { Component, Inject, OnInit } from '@angular/core';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material';

import { filter, tap } from 'rxjs/operators';

import { DateUtils } from '../../common/utils/date-utils';
import { LoadingDialogComponent } from '../../common/components/loading-dialog/loading-dialog.component';
import { LoadingService } from '../../common/service/loading.service';
import { BudgetService } from '../../common/service/budget.service';
import { CurrencyUtils } from '../../common/utils/currency-utils';
import { ProfileService } from '../../common/service/profile.service';

@Component({
  selector: 'bk-move-goal-dialog',
  templateUrl: './move-goal-dialog.component.html',
  styleUrls: ['./move-goal-dialog.component.css']
})
export class MoveGoalDialogComponent implements OnInit {
  public months: string[] = DateUtils.MONTHS;
  public selectedYear: number;
  public selectedMonth: number;
  public moveValue: number;
  public errors: string;

  public constructor(
    @Inject(MAT_DIALOG_DATA) public data: {type: string, budgetType: string, category: BudgetCategory, budget: Budget, goal: BudgetGoal},
    private _dialogRef: MatDialogRef<MoveGoalDialogComponent>,
    private _loadingService: LoadingService,
    private _budgetService: BudgetService,
    private _profileService: ProfileService
  ) {}

  public ngOnInit(): void {
    const balance: BudgetBalance = this.data.goal.balance;
    this.moveValue = balance.completeValue - balance.value;
    if (this.data.budget.month < 12) {
      this.selectedYear = this.data.budget.year;
      this.selectedMonth = this.data.budget.month + 1;
    } else {
      this.selectedYear = this.data.budget.year++;
      this.selectedMonth = 1;
    }
  }

  public close(refreshBudget: boolean): void {
    this._dialogRef.close(refreshBudget);
  }

  public save(): void {
    this.errors = null;
    const now: Date = new Date();
    if (this.selectedYear === this.data.budget.year && this.selectedMonth === this.data.budget.month) {
      this.errors = 'Выберите другой месяц';
    }
    if (this.selectedYear < now.getFullYear() || (this.selectedYear === now.getFullYear() && this.selectedMonth < now.getMonth() + 1)) {
      this.errors = 'Выбранный месяц завершен';
    }
    if (this.moveValue <= 0) {
      this.errors = 'Неверная сумму остатка';
    }
    if (this.moveValue <= 0 || this.moveValue >= 10000000) {
      this.errors = `Недопустимое значение для ${CurrencyUtils.convertCodeToSymbol(this._profileService.getCurrencyDetails(this.data.goal.balance.currency).symbol)}`;
    }

    if (!this.errors) {
      const loadingDialog: MatDialogRef<LoadingDialogComponent> = this._loadingService.openLoadingDialog('Сохранение...');
      this._budgetService.moveGoal(this.data.budget.id, this.data.type, this.data.category.title, this.data.goal.title, this.selectedYear, this.selectedMonth, this.moveValue)
        .pipe(
          tap(simpleResponse => {
            loadingDialog.close();
            if (simpleResponse.status === 'FAIL') {
              console
              this.errors = simpleResponse.message === 'ALREADY_EXIST' ? 'Цель существует в выбранном месяце' : 'Ошибка при сохранении';
            }
          }),
          filter(simpleResponse => simpleResponse.status === 'SUCCESS'),
        ).subscribe(() => this._dialogRef.close(true));
    }
  }

  public changeMonth(monthIndex: number): void {
    this.selectedMonth = monthIndex;
  }

  public changeYear(date: {year: number, month: number}): void {
    this.selectedMonth = date.month;
    this.selectedYear = date.year;
  }
}
