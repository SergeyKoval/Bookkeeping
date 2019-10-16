import { Component, Inject, OnInit } from '@angular/core';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material';
import { MatDialog } from '@angular/material/dialog';

import { filter } from 'rxjs/operators';

import { GoalWrapper } from '../../common/model/budget/GoalWrapper';
import { MoveGoalDialogComponent } from '../move-goal-dialog/move-goal-dialog.component';
import { DateUtils } from '../../common/utils/date-utils';
import { PlanBudgetDialogComponent } from '../plan-budget-dialog/plan-budget-dialog.component';
import { CloseMonthFilter } from '../../common/model/budget/CloseMonthFilter';
import { ConfirmDialogService } from '../../common/components/confirm-dialog/confirm-dialog.service';
import { BudgetService } from '../../common/service/budget.service';
import { CategoryWrapper } from '../../common/model/budget/CategoryWrapper';
import { ProfileService } from '../../common/service/profile.service';
import { CategoryStatisticsDialogComponent } from '../category-statistics-dialog/category-statistics-dialog.component';
import { LoadingDialogComponent } from '../../common/components/loading-dialog/loading-dialog.component';
import { LoadingService } from '../../common/service/loading.service';
import { AlertType } from '../../common/model/alert/AlertType';
import { AlertService } from '../../common/service/alert.service';

@Component({
  selector: 'bk-close-month-dialog',
  templateUrl: './close-month-dialog.component.html',
  styleUrls: ['./close-month-dialog.component.css']
})
export class CloseMonthDialogComponent implements OnInit {
  public FILTER: typeof CloseMonthFilter = CloseMonthFilter;
  public months: string[] = DateUtils.MONTHS;
  public type: string = 'period';
  public goals: GoalWrapper[] = [];
  public categories: CategoryWrapper[] = [];
  public goalFilters: CloseMonthFilter[] = [];
  public categoryFilters: CloseMonthFilter[] = [];
  public nextMonthPeriod: {year: number, month: number};
  public errorMessage: string;
  public nextPeriodBudget: Budget;

  public constructor(
    @Inject(MAT_DIALOG_DATA) public data: {budget: Budget},
    private _dialogRef: MatDialogRef<CloseMonthDialogComponent>,
    private _dialog: MatDialog,
    private _confirmDialogService: ConfirmDialogService,
    private _profileService: ProfileService,
    private _budgetService: BudgetService,
    private _loadingService: LoadingService,
    private _alertService: AlertService
  ) {}

  public ngOnInit(): void {
    const today: Date = new Date();
    const todayYear: number = today.getFullYear();
    const todayMonth: number = today.getMonth() + 1;

    this.nextMonthPeriod = (this.data.budget.year !== todayYear || this.data.budget.month !== todayMonth)
      ? {year: todayYear, month: todayMonth} : DateUtils.nextMonthPeriod(todayYear, todayMonth);
  }

  public calculateGoalPercentDone(goal: BudgetGoal): number {
    if (goal.done) {
      return 100;
    }

    const percent: number = Math.round(goal.balance.value / goal.balance.completeValue * 100);
    return percent > 100 ? 100 : percent;
  }

  public moveGoal(goalWrapper: GoalWrapper): void {
    this._dialog.open(MoveGoalDialogComponent, {
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

  public excludeCategory(categoryWrapper: CategoryWrapper): void {
    categoryWrapper.actionPlan = null;
  }

  public showStatistics(categoryWrapper: CategoryWrapper): void {
    this._dialog.open(CategoryStatisticsDialogComponent, {
      id: 'category-statistics-dialog',
      panelClass: 'budget-plan-dialog',
      width: '450px',
      position: {top: 'top'},
      data: {
        'budgetType': categoryWrapper.type,
        'category': categoryWrapper.category.title,
        'year': this.nextMonthPeriod.year,
        'month': this.nextMonthPeriod.month
      }
    }).afterClosed()
      .subscribe(potentialActionPlan => this.processPotentialCategoryActionPlan(categoryWrapper, potentialActionPlan));
  }

  public repeatGoal(goalWrapper: GoalWrapper): void {
    const value: number = goalWrapper.goal.balance.value;
    const completeValue: number = goalWrapper.goal.balance.completeValue;
    const repeatValue: number = value > completeValue ? value : completeValue;
    this._dialog.open(PlanBudgetDialogComponent, {
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
          'month': this.nextMonthPeriod.month,
          'year': this.nextMonthPeriod.year,
          'balance': {'completeValue': repeatValue, 'currency': goalWrapper.goal.balance.currency}
        }, 'postpone': true
      }
    }).afterClosed()
      .subscribe((actionPlan: CloseMonthGoalPlan) => this.processGoalActionPlanResult(goalWrapper, actionPlan));
  }

  public repeatCategory(categoryWrapper: CategoryWrapper): void {
    const categoryBalance: {[currency: string]: BudgetBalance} = Object.assign({}, categoryWrapper.category.balance);
    Object.keys(categoryBalance).forEach(currency => {
      const balance: BudgetBalance = categoryBalance[currency];
      if (balance.value > balance.completeValue) {
        balance.completeValue = balance.value;
      }
    });

    this._dialog.open(PlanBudgetDialogComponent, {
      panelClass: 'budget-plan-dialog',
      width: '400px',
      position: {top: 'top'},
      data: {
        'editMode': true,
        'type': 'category',
        'budgetType': categoryWrapper.type,
        'categoryTitle': categoryWrapper.category.title,
        'postpone': true,
        'budget': this.nextPeriodBudget,
        'categoryBalance': categoryBalance
      }
    }).afterClosed()
      .subscribe(potentialActionPlan => this.processPotentialCategoryActionPlan(categoryWrapper, potentialActionPlan));
  }

  public editCategory(categoryWrapper: CategoryWrapper): void {
    this._dialog.open(PlanBudgetDialogComponent, {
      panelClass: 'budget-plan-dialog',
      width: '400px',
      position: {top: 'top'},
      data: {
        'editMode': true,
        'type': 'category',
        'budgetType': categoryWrapper.type,
        'categoryTitle': categoryWrapper.category.title,
        'postpone': true,
        'budget': this.nextPeriodBudget,
        'categoryBalance': Object.assign({}, categoryWrapper.actionPlan)
      }
    }).afterClosed()
      .subscribe(potentialActionPlan => this.processPotentialCategoryActionPlan(categoryWrapper, potentialActionPlan));
  }

  public editGoal(goalWrapper: GoalWrapper): void {
    this._dialog.open(PlanBudgetDialogComponent, {
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
        'postpone': true,
        'disableChangeGoalMonth': true
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

  public back(): void {
    const backType: string = this.type === 'goals' || this.goals.length === 0 ? 'period' : 'goals';
    this._confirmDialogService.openConfirmDialog(`Вернуться к ${backType === 'period' ? 'выбору периода' : 'целям'}`
      , `При возврате, все изменения, сделанные в ${backType === 'period' ? 'целях' : 'категориях'}, будут потеряны. Продолжить?`)
      .afterClosed()
      .pipe(filter((result: boolean) => result === true))
      .subscribe(() => {
        if (backType === 'period') {
          this.goals = [];
          this.nextPeriodBudget = null;
          this.goalFilters = [];
          this.errorMessage = null;
        }
        this.categories = [];
        this.type = backType;
      });
  }

  public next(): void {
    if (this.type === 'period') {
      const today: Date = new Date();
      const year: number = today.getFullYear();
      if (this.nextMonthPeriod.year < year
        || (this.nextMonthPeriod.year === year && this.nextMonthPeriod.month <= today.getMonth())
        || (this.data.budget.year === this.nextMonthPeriod.year && this.data.budget.month === this.nextMonthPeriod.month)) {
        this.errorMessage = 'Выбранный период нельзя спланировать';
        return;
      }
      const loadingDialog: MatDialogRef<LoadingDialogComponent> = this._loadingService.openLoadingDialog('Загрузка планируемого периода...');
      this._budgetService.loadBudget(this.nextMonthPeriod.year, this.nextMonthPeriod.month).subscribe(nextPeriodBudget => {
        this.nextPeriodBudget = nextPeriodBudget;
        this.processBudgetGoals(nextPeriodBudget.income, this.data.budget.income, 'income');
        this.processBudgetGoals(nextPeriodBudget.expense, this.data.budget.expense, 'expense');
        this.type = 'goals';
        if (this.goals.length === 0) {
          this.nextToCategories();
        } else if (this.goals.filter(goalWrapper => !goalWrapper.goal.done).length > 0) {
          this.goalFilters.push(CloseMonthFilter.UNDONE);
        }

        loadingDialog.close();
      });
    } else {
      this.nextToCategories();
    }
  }

  public getFilters(): CloseMonthFilter[] {
    return this.type === 'goals' ? this.goalFilters : this.categoryFilters;
  }

  public getCategoryIcon(category: string): string {
    return this._profileService.getCategoryIcon(category);
  }

  public getNumberOfLinesInCategory(categoryWrapper: CategoryWrapper): number {
    let lines: number = Object.keys(categoryWrapper.category.balance).length;
    if (categoryWrapper.actionPlan) {
      const actionPlanLines: number = Object.keys(categoryWrapper.actionPlan).length;
      if (actionPlanLines > lines) {
        lines = actionPlanLines;
      }
    }

    return lines > 0 ? lines : 1;
  }

  public calculateCategoryPercentDone(categoryWrapper: CategoryWrapper): number {
    return this._budgetService.calculatePercentDone(categoryWrapper.category.balance);
  }

  public close(): void {
    this._dialogRef.close(false);
  }

  public save(): void {
    const planingCategories: CategoryWrapper[] = this.categories.filter(categoryWrapper => categoryWrapper.actionPlan);
    const anotherMontGoals: GoalWrapper[] = this.goals.filter(goalWrapper => {
      return goalWrapper.actionPlan && (goalWrapper.actionPlan.year !== this.nextPeriodBudget.year || goalWrapper.actionPlan.month !== this.nextPeriodBudget.month);
    });
    const loadingDialog: MatDialogRef<LoadingDialogComponent> = this._loadingService.openLoadingDialog('Сохранение...');
    this._budgetService.closeMonth(this.nextPeriodBudget.year, this.nextPeriodBudget.month, planingCategories, anotherMontGoals)
      .subscribe((simpleResponse: SimpleResponse) => {
        loadingDialog.close();
        if (simpleResponse.status === 'SUCCESS') {
          this._alertService.addAlert(AlertType.SUCCESS, 'Бюджет успешно запланирован');
        } else {
          const errors: string[] = simpleResponse.result['errors'];
          errors.forEach(errorMessage => this._alertService.addAlert(AlertType.WARNING, errorMessage));
        }
        this._dialogRef.close(true);
      });
  }

  public changeMonth (month: number): void {
    this.nextMonthPeriod.month = month;
  }

  public changeYear (nextMonthPeriod: {year: number; month: number}): void {
    this.nextMonthPeriod = nextMonthPeriod;
  }

  private nextToCategories(): void {
    this.processBudgetCategories(this.nextPeriodBudget.income, this.data.budget.income, 'income');
    this.processBudgetCategories(this.nextPeriodBudget.expense, this.data.budget.expense, 'expense');
    this.type = 'categories';
  }

  private processPotentialCategoryActionPlan(categoryWrapper: CategoryWrapper, potentialActionPlan: BudgetBalance[]): void {
    const actionPlan: {[currency: string]: BudgetBalance} = {};
    potentialActionPlan.forEach(balanceItem => {
      if (balanceItem.completeValue > 0) {
        actionPlan[balanceItem.currency] = balanceItem;
      }
    });

    categoryWrapper.actionPlan = Object.keys(actionPlan).length > 0 ? actionPlan : null;
  }

  private isSuitableGoalWrapper(goalWrapper: GoalWrapper, type: string, categoryTitle: string): boolean {
    return goalWrapper.actionPlan
      && goalWrapper.actionPlan.year === this.nextPeriodBudget.year
      && goalWrapper.actionPlan.month === this.nextPeriodBudget.month
      && goalWrapper.type === type && goalWrapper.category === categoryTitle;
  }

  private processBudgetCategories(nextPeriodBudgetDetails: BudgetDetails, budgetDetails: BudgetDetails, type: string): void {
    const categoryMap: Map<string, CategoryWrapper> = new Map<string, CategoryWrapper>();
    budgetDetails.categories.forEach(category => {
      const goalWrappers: GoalWrapper[] = this.goals.filter(goalWrapper => this.isSuitableGoalWrapper(goalWrapper, type, category.title));
      categoryMap.set(category.title, new CategoryWrapper(category, type, goalWrappers, goalWrappers.length === 0));
    });

    nextPeriodBudgetDetails.categories.forEach(category => {
      if (!categoryMap.has(category.title)) {
        const categoryGoals: GoalWrapper[] = this.goals.filter(goalWrapper => this.isSuitableGoalWrapper(goalWrapper, type, category.title));
        categoryMap.set(category.title, new CategoryWrapper(category, type, categoryGoals, false, true, category.balance));
      } else {
        categoryMap.get(category.title).removable = false;
        categoryMap.get(category.title).actionPlan = category.balance;
      }
    });

    Array.from(categoryMap.values()).forEach((categoryWrapper: CategoryWrapper)  => {
      if (categoryWrapper.goalWrappers.length > 0) {
        categoryWrapper.goalWrappers.forEach(goalWrapper => {
          if (goalWrapper.removable) {
            const goalCurrency: string = goalWrapper.actionPlan.balance.currency;
            if (!categoryWrapper.actionPlan) {
              categoryWrapper.actionPlan = {};
            }
            if (!categoryWrapper.actionPlan.hasOwnProperty(goalCurrency)) {
              categoryWrapper.actionPlan[goalCurrency] = {currency: goalCurrency};
            }

            categoryWrapper.actionPlan[goalCurrency].completeValue = goalWrapper.actionPlan.balance.completeValue + (categoryWrapper.actionPlan[goalCurrency].completeValue | 0);
          }
        });
      }

      this.categories.push(categoryWrapper);
    });
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
          year: this.nextMonthPeriod.year,
          month: this.nextMonthPeriod.month,
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
