import { Component, Inject, OnInit } from '@angular/core';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material';

import { GoalWrapper } from '../../common/model/budget/GoalWrapper';
import { MoveGoalDialogComponent } from '../move-goal-dialog/move-goal-dialog.component';
import { DialogService } from '../../common/service/dialog.service';
import { DateUtils } from '../../common/utils/date-utils';
import { PlanBudgetDialogComponent } from '../plan-budget-dialog/plan-budget-dialog.component';
import { GoalFilter } from '../../common/model/budget/GoalFilter';

@Component({
  selector: 'bk-close-month-dialog',
  templateUrl: './close-month-dialog.component.html',
  styleUrls: ['./close-month-dialog.component.css']
})
export class CloseMonthDialogComponent implements OnInit {
  public GOAL_FILTER: typeof GoalFilter = GoalFilter;
  public months: string[] = DateUtils.MONTHS;
  public type: string = 'goals';
  public goals: GoalWrapper[] = [];
  public goalFilters: GoalFilter[] = [];

  public constructor(
    @Inject(MAT_DIALOG_DATA) public data: {budget: Budget},
    private _dialogRef: MatDialogRef<CloseMonthDialogComponent>,
    private _dialogService: DialogService
  ) {}

  public ngOnInit(): void {
    this.processBudgetGoals(this.data.budget.income, 'income');
    this.processBudgetGoals(this.data.budget.expense, 'expense');
    if (this.goals.length === 0) {
      this.type = 'categories';
    } else if (this.goals.filter(goalWrapper => !goalWrapper.goal.done).length > 0) {
      this.goalFilters.push(GoalFilter.UNDONE);
    }
  }

  public onChangeSelectedType(selectedType: string): void {
    this.type = selectedType;
  }

  public calculateGoalPercentDone(goal: BudgetGoal): number {
    if (goal.done) {
      return 100;
    }

    const percent: number = Math.round(goal.balance.value / goal.balance.completeValue * 100);
    return percent > 100 ? 100 : percent;
  }

  public moveGoal(goalWrapper: GoalWrapper): void {
    this._dialogService.openDialog(MoveGoalDialogComponent, {
      panelClass: 'move-goal-dialog',
      width: '400px',
      position: {top: 'top'},
      data: {
        'type': goalWrapper.type,
        'category': goalWrapper.category,
        'goal': goalWrapper.goal,
        'budget': this.data.budget,
        'postpone': true
      }
    }).afterClosed()
      .subscribe((actionPlan: CloseMonthGoalPlan) => this.processGoalActionPlanResult(goalWrapper, actionPlan));
  }

  public getMonthTitle(monthIndex: number): string {
    return this.months[monthIndex - 1];
  }

  public excludeGoal(goalWrapper: GoalWrapper): void {
    goalWrapper.actionPlan = null;
  }

  public repeatGoal(goalWrapper: GoalWrapper): void {
    const today: Date = new Date();
    const nextMonthPeriod: {year: number, month: number} = DateUtils.nextMonthPeriod(today.getFullYear(), today.getMonth() + 1);
    this._dialogService.openDialog(PlanBudgetDialogComponent, {
      panelClass: 'budget-plan-dialog',
      width: '400px',
      position: {top: 'top'},
      data: {
        'editMode': true,
        'type': 'goal',
        'budgetType': goalWrapper.type,
        'goal': goalWrapper.goal,
        'budget': this.data.budget,
        'categoryTitle': goalWrapper.category,
        'closeMonthGoalPlan': {
          'month': nextMonthPeriod.month,
          'year': nextMonthPeriod.year,
          'balance': {'completeValue': goalWrapper.goal.balance.completeValue, 'currency': goalWrapper.goal.balance.currency}
        }, 'postpone': true
      }
    }).afterClosed()
      .subscribe((actionPlan: CloseMonthGoalPlan) => this.processGoalActionPlanResult(goalWrapper, actionPlan));
  }

  public editGoal(goalWrapper: GoalWrapper): void {
    this._dialogService.openDialog(PlanBudgetDialogComponent, {
      panelClass: 'budget-plan-dialog',
      width: '400px',
      position: {top: 'top'},
      data: {
        'editMode': true,
        'type': 'goal',
        'budgetType': goalWrapper.type,
        'goal': goalWrapper.goal,
        'budget': this.data.budget,
        'categoryTitle': goalWrapper.category,
        'closeMonthGoalPlan': goalWrapper.actionPlan,
        'postpone': true
      }
    }).afterClosed()
      .subscribe((actionPlan: CloseMonthGoalPlan) => this.processGoalActionPlanResult(goalWrapper, actionPlan));
  }

  public isChecked(goalFilter: GoalFilter): boolean {
    return this.goalFilters.includes(goalFilter);
  }

  public clickGoalFilter(goalFilter: GoalFilter, oppositeGoalFilter: GoalFilter): void {
    if (this.isChecked(goalFilter)) {
      this.goalFilters = this.goalFilters.filter(filter => filter !== goalFilter);
    } else {
      this.goalFilters = this.goalFilters.concat([goalFilter]);
      if (this.isChecked(oppositeGoalFilter)) {
        this.goalFilters = this.goalFilters.filter(filter => filter !== oppositeGoalFilter);
      }
    }
  }

  public close(): void {
    this._dialogRef.close();
  }

  private processBudgetGoals(budgetDetails: BudgetDetails, type: string): void {
    budgetDetails.categories.forEach(category => {
      category.goals.forEach(goal => {
        this.goals.push(new GoalWrapper(goal, type, category.title));
      });
    });
  }

  private processGoalActionPlanResult(goalWrapper: GoalWrapper, actionPlan: CloseMonthGoalPlan): void {
    if (actionPlan) {
      goalWrapper.actionPlan = actionPlan;
      this.goals = Object.assign([], this.goals);
    }
  }
}
