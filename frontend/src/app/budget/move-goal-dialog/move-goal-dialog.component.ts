import { Component, Inject, OnInit } from '@angular/core';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material';

import { DateUtils } from '../../common/utils/date-utils';

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

  public constructor(
    @Inject(MAT_DIALOG_DATA) public data: {type: string, budgetType: string, category: BudgetCategory, budget: Budget, goal: BudgetGoal},
    private _dialogRef: MatDialogRef<MoveGoalDialogComponent>
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

  }

  public changeMonth(monthIndex: number): void {
    this.selectedMonth = monthIndex;
  }

  public changeYear(date: {year: number, month: number}): void {
    this.selectedMonth = date.month;
    this.selectedYear = date.year;
  }
}
