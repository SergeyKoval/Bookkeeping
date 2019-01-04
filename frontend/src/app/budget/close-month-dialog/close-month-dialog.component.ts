import { Component, Inject, OnInit } from '@angular/core';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material';

import { filter } from 'rxjs/operators';

import { GoalWrapper } from '../../common/model/budget/GoalWrapper';
import { MoveGoalDialogComponent } from '../move-goal-dialog/move-goal-dialog.component';
import { DialogService } from '../../common/service/dialog.service';
import { DateUtils } from '../../common/utils/date-utils';
import { PlanBudgetDialogComponent } from '../plan-budget-dialog/plan-budget-dialog.component';
import { CloseMonthFilter } from '../../common/model/budget/CloseMonthFilter';
import { ConfirmDialogService } from '../../common/components/confirm-dialog/confirm-dialog.service';
import { BudgetService } from '../../common/service/budget.service';

@Component({
  selector: 'bk-close-month-dialog',
  templateUrl: './close-month-dialog.component.html',
  styleUrls: ['./close-month-dialog.component.css']
})
export class CloseMonthDialogComponent implements OnInit {
  public FILTER: typeof CloseMonthFilter = CloseMonthFilter;
  public months: string[] = DateUtils.MONTHS;
  public type: string = 'goals';
  public goals: GoalWrapper[] = [];
  public goalFilters: CloseMonthFilter[] = [];
  public categoryFilters: CloseMonthFilter[] = [];

  public constructor(
    @Inject(MAT_DIALOG_DATA) public data: {budget: Budget, nextPeriodBudget: Budget, nextMonthPeriod: {year: number, month: number}},
    private _dialogRef: MatDialogRef<CloseMonthDialogComponent>,
    private _dialogService: DialogService,
    private _confirmDialogService: ConfirmDialogService,

    private _budgetService: BudgetService
  ) {}

  public ngOnInit(): void {
    this.processBudgetGoals(this.data.nextPeriodBudget.income, this.data.budget.income, 'income');
    this.processBudgetGoals(this.data.nextPeriodBudget.expense, this.data.budget.expense, 'expense');
    if (this.goals.length === 0) {
      this.nextToCategories();
    } else if (this.goals.filter(goalWrapper => !goalWrapper.goal.done).length > 0) {
      this.goalFilters.push(CloseMonthFilter.UNDONE);
    }
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
          'month': this.data.nextMonthPeriod.month,
          'year': this.data.nextMonthPeriod.year,
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

  public isChecked(goalFilter: CloseMonthFilter): boolean {
    const filters: CloseMonthFilter[] = this.getFilters();
    return filters.includes(goalFilter);
  }

  public clickFilter(goalFilter: CloseMonthFilter, oppositeGoalFilter: CloseMonthFilter): void {
    let filters: CloseMonthFilter[] = this.getFilters();
    if (this.isChecked(goalFilter)) {
      filters = filters.filter(checkedFilter => checkedFilter !== goalFilter);
    } else {
      filters = filters.concat([goalFilter]);
      if (this.isChecked(oppositeGoalFilter)) {
        filters = filters.filter(oppositeCheckedFilter => oppositeCheckedFilter !== oppositeGoalFilter);
      }
    }

    if (this.type === 'goals') {
      this.goalFilters = filters;
    } else {
      this.categoryFilters = filters;
    }
  }

  public backToGoals(): void {
    this._confirmDialogService.openConfirmDialog('Вернуться к целям', 'При возврате, все изменения, сделанные в категориях, будут потеряны. Продолжить?')
      .afterClosed()
      .pipe(filter((result: boolean) => result === true))
      .subscribe(() => this.type = 'goals');
  }

  public nextToCategories (): void {
    this.type = 'categories';
  }

  public getFilters(): CloseMonthFilter[] {
    return this.type === 'goals' ? this.goalFilters : this.categoryFilters;
  }

  public close(): void {
    this._dialogRef.close();
  }

  private processBudgetGoals(nextPeriodBudgetDetails: BudgetDetails, budgetDetails: BudgetDetails, type: string): void {
    const categoryMap: Map<string, Map<string, GoalWrapper>> = new Map<string, Map<string, GoalWrapper>>();
    budgetDetails.categories.forEach(category => {
      const goalMap: Map<string, GoalWrapper> = new Map<string, GoalWrapper>();
      categoryMap.set(category.title, goalMap);
      category.goals.forEach(goal => {
        goalMap.set(goal.title, new GoalWrapper(goal, type, category.title));
      });
    });

    nextPeriodBudgetDetails.categories.forEach(category => {
      if (!categoryMap.has(category.title)) {
        categoryMap.set(category.title, new Map<string, GoalWrapper>());
      }
      const goalMap: Map<string, GoalWrapper> = categoryMap.get(category.title);
      category.goals.forEach(goal => {
        const actionPlan: CloseMonthGoalPlan = {
          year: this.data.nextMonthPeriod.year,
          month: this.data.nextMonthPeriod.month,
          balance: {currency: goal.balance.currency, completeValue: goal.balance.completeValue}
        };
        if (goalMap.has(goal.title)) {
          const goalWrapper: GoalWrapper = goalMap.get(goal.title);
          goalWrapper.removable = false;
          goalWrapper.actionPlan = actionPlan;
        } else {
          const newGoalWrapper: GoalWrapper = new GoalWrapper(goal, type, category.title, false, true);
          newGoalWrapper.actionPlan = actionPlan;
          goalMap.set(goal.title, newGoalWrapper);
        }
      });
    });

    Array.from(categoryMap.values()).forEach((goalsMap: Map<string, GoalWrapper>)  => {
      Array.from(goalsMap.values()).forEach(goalWrapper => this.goals.push(goalWrapper));
    });
  }

  private processGoalActionPlanResult(goalWrapper: GoalWrapper, actionPlan: CloseMonthGoalPlan): void {
    if (actionPlan) {
      goalWrapper.actionPlan = actionPlan;
      this.goals = Object.assign([], this.goals);
    }
  }
}
