import { Component, Input, OnInit, Output } from '@angular/core';
import { MatDialogRef } from '@angular/material';

import { filter, switchMap, tap } from 'rxjs/internal/operators';
import { Observable, Subject } from 'rxjs';

import { ProfileService } from '../../common/service/profile.service';
import { CurrencyService } from '../../common/service/currency.service';
import { BudgetService } from '../../common/service/budget.service';
import { LoadingDialogComponent } from '../../common/components/loading-dialog/loading-dialog.component';
import { LoadingService } from '../../common/service/loading.service';
import { AlertService } from '../../common/service/alert.service';
import { AlertType } from '../../common/model/alert/AlertType';
import { PlanBudgetDialogComponent } from '../plan-budget-dialog/plan-budget-dialog.component';
import { DialogService } from '../../common/service/dialog.service';
import { ConfirmDialogService } from '../../common/components/confirm-dialog/confirm-dialog.service';
import { MoveGoalDialogComponent } from '../move-goal-dialog/move-goal-dialog.component';

@Component({
  selector: 'bk-budget-details',
  templateUrl: './budget-details.component.html',
  styleUrls: ['./budget-details.component.css']
})
export class BudgetDetailsComponent implements OnInit {
  @Input()
  public budget: Budget;
  @Input()
  public type: string;
  @Input()
  public monthProgress: MonthProgress;

  @Output()
  public updateBudget: Subject<boolean> = new Subject();

  public budgetDetails: BudgetDetails;
  public hasGoals: boolean;
  public goalsCount: number;
  public goalsDone: number;

  public constructor(
    private _profileService: ProfileService,
    private _currencyService: CurrencyService,
    private _budgetService: BudgetService,
    private _loadingService: LoadingService,
    private _alertService: AlertService,
    private _dialogService: DialogService,
    private _confirmDialogService: ConfirmDialogService
  ) {}

  public ngOnInit(): void {
    this.budgetDetails = this.budget[this.type];
    this.calculateGoalCounts();
  }

  public getBudgetPercentDone(): number {
    return this._budgetService.calculatePercentDone(this.budget[this.type].balance);
  }

  public getBudgetFullPercentDone(): number {
    return this._budgetService.calculatePercentDone(this.budget[this.type].balance, 0, true);
  }

  public getNumberOfCurrencies(balance: {[currency: string]: BudgetBalance}): number {
    const numberOfCurrencies: number = Object.keys(balance).length;
    return numberOfCurrencies > 0 ? numberOfCurrencies : 1;
  }

  public getCategoryIcon(category: string): string {
    return this._profileService.getCategoryIcon(category);
  }

  public calculateGoalPercentDone(goal: BudgetGoal): number {
    if (goal.done) {
      return 100;
    }

    const percent: number = Math.round(goal.balance.value / goal.balance.completeValue * 100);
    return percent > 100 ? 100 : percent;
  }

  public calculateCategoryPercentDone(category: BudgetCategory): number {
    return this._budgetService.calculatePercentDone(category.balance);
  }

  public calculateCategoryFullPercentDone(category: BudgetCategory): number {
    return this._budgetService.calculatePercentDone(category.balance, 0, true);
  }

  public calculateGoalStyle(goal: BudgetGoal, goalPercent: number): string {
    if (goal.done) {
      return 'progress-bar-success';
    }

    return this.calculateStyle(goalPercent);
  }

  public calculateStyle(percent: number): string {
    if (this.monthProgress.currentMonth) {
      if (this.type === 'income') {
        return percent < this.monthProgress.monthPercent ? 'progress-bar-warning' : 'progress-bar-success';
      } else {
        return percent < this.monthProgress.monthPercent ? 'progress-bar-success' : 'progress-bar-warning';
      }
    } else {
      if (this.type === 'income') {
        return percent < 90 ? 'progress-bar-warning' : 'progress-bar-success';
      } else {
        return percent < 90 ? 'progress-bar-success' : 'progress-bar-warning';
      }
    }
  }

  public clickGoalDone(category: BudgetCategory, goal: BudgetGoal): void {
    const mdDialogRef: MatDialogRef<LoadingDialogComponent> = this._loadingService.openLoadingDialog('Изменение статуса цели...');
    this._budgetService.changeGoalDoneStatus(this.budget.id, this.type, category.title, goal.title, !goal.done)
      .pipe(
        tap(simpleResponse => {
          if (simpleResponse.status === 'FAIL') {
            this._alertService.addAlert(AlertType.WARNING, 'Ошибка при изменении статуса цели');
            mdDialogRef.close();
          }
        }),
        filter(simpleResponse => simpleResponse.status === 'SUCCESS'),
      ).subscribe(() => {
        goal.done = !goal.done;
        this.calculateGoalCounts();
        mdDialogRef.close();
        this._alertService.addAlert(AlertType.SUCCESS, 'Статус цели изменен');
      });
  }

  public toggleBudgetDetails(): void {
    this.budgetDetails.opened = !this.budgetDetails.opened;
    this._budgetService.toggleBudgetDetails(this.budget.id, this.type, this.budgetDetails.opened).subscribe(simpleResponse => {
      if (simpleResponse.status === 'FAIL') {
        this._alertService.addAlert(AlertType.WARNING, 'Ошибка при отправке данных на сервер');
      }
    });
  }

  public openCategoryEditDialog(category: BudgetCategory): void {
    this._dialogService.openDialog(PlanBudgetDialogComponent, {
      panelClass: 'budget-plan-dialog',
      width: '400px',
      position: {top: 'top'},
      data: {
        'editMode': true,
        'type': 'category',
        'budgetType': this.type,
        'category': category,
        'budget': this.budget
      }
    }).afterClosed()
      .subscribe(refreshBudget => {
        this.updateBudget.next(refreshBudget);
      });
  }

  public openGoalEditDialog(category: BudgetCategory, goal: BudgetGoal): void {
    this._dialogService.openDialog(PlanBudgetDialogComponent, {
      panelClass: 'budget-plan-dialog',
      width: '400px',
      position: {top: 'top'},
      data: {
        'editMode': true,
        'type': 'goal',
        'budgetType': this.type,
        'category': category,
        'goal': goal,
        'budget': this.budget
      }
    }).afterClosed()
      .subscribe(refreshBudget => {
        this.updateBudget.next(refreshBudget);
      });
  }

  public moveGoal(category: BudgetCategory, goal: BudgetGoal): void {
    if (goal.done) {
      this._alertService.addAlert(AlertType.INFO, 'Цель уже выполнена');
      return;
    }

    if (goal.balance.value === 0) {
      this.openGoalEditDialog(category, goal);
      return;
    }

    this._dialogService.openDialog(MoveGoalDialogComponent, {
      panelClass: 'move-goal-dialog',
      width: '400px',
      position: {top: 'top'},
      data: {
        'type': this.type,
        'category': category,
        'goal': goal,
        'budget': this.budget
      }
    }).afterClosed()
      .subscribe(refreshBudget => {
        this.updateBudget.next(refreshBudget);
      });
  }

  public showRemoveButton(balance: {[currency: string]: BudgetBalance}): boolean {
    return Object.values(balance).filter(budgetBalance => budgetBalance.value > 0).length === 0;
  }

  public removeGoal(category: BudgetCategory, goal: BudgetGoal): void {
    this.removeItem('goal', this._budgetService.removeGoal(this.budget.id, this.type, category.title, goal.title));
  }

  public removeCategory(category: BudgetCategory): void {
    this.removeItem('category', this._budgetService.removeCategory(this.budget.id, this.type, category.title));
  }

  public isSingleCurrency(): boolean {
    return Object.keys(this.budgetDetails.balance).length === 1;
  }

  private removeItem(removeType: string, callback: Observable<SimpleResponse>): void {
    let loadingDialog: MatDialogRef<LoadingDialogComponent>;
    this._confirmDialogService.openConfirmDialog(`Удаление ${removeType === 'category' ? 'категории' : 'цели'}`, `Уверены что хотите удалить ${removeType === 'category' ? 'категорию' : 'цель'}`)
      .afterClosed()
      .pipe(
        filter((result: boolean) => result === true),
        tap(() => loadingDialog = this._loadingService.openLoadingDialog(`Удаление ${removeType === 'category' ? 'категории' : 'цели'}...`)),
        switchMap(() => callback)
      ).subscribe((response: SimpleResponse) => {
      loadingDialog.close();
      if (response.status === 'SUCCESS') {
        this._alertService.addAlert(AlertType.SUCCESS, `${removeType === 'category' ? 'Категория' : 'Цель'} удалена`);
        this.updateBudget.next(true);
      } else {
        this._alertService.addAlert(AlertType.DANGER, `Ошибка при удалении ${removeType === 'category' ? 'категории' : 'цели'}`);
      }
    });
  }

  private calculateGoalCounts(): void {
    this.goalsDone = 0;
    this.goalsCount = 0;
    this.hasGoals = false;

    this.budgetDetails.categories.forEach(category => {
      if (category.goals && category.goals.length > 0) {
        this.hasGoals = true;
        this.goalsCount = this.goalsCount + category.goals.length;
        category.goals.forEach(goal => {
          if (goal.done) {
            this.goalsDone++;
          }
        });
      }
    });
  }
}
