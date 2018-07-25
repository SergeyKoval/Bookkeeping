import { Component, Input, OnInit } from '@angular/core';
import { MatDialogRef } from '@angular/material';

import { filter, tap } from 'rxjs/internal/operators';

import { ProfileService } from '../../common/service/profile.service';
import { CurrencyService } from '../../common/service/currency.service';
import { BudgetService } from '../../common/service/budget.service';
import { LoadingDialogComponent } from '../../common/components/loading-dialog/loading-dialog.component';
import { LoadingService } from '../../common/service/loading.service';
import { AlertService } from '../../common/service/alert.service';
import { AlertType } from '../../common/model/alert/AlertType';

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

  public budgetDetails: BudgetDetails;
  public hasGoals: boolean = false;
  public goalsCount: number = 0;
  public goalsDone: number = 0;

  public constructor(
    private _profileService: ProfileService,
    private _currencyService: CurrencyService,
    private _budgetService: BudgetService,
    private _loadingService: LoadingService,
    private _alertService: AlertService
  ) {}

  public ngOnInit(): void {
    this.budgetDetails = this.budget[this.type];
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
    let value: number = 0;
    let completeValue: number = 0;
    const balance: {[currency: string]: BudgetBalance} = category.balance;
    const currencies: string[] = Object.keys(balance);

    if (currencies.length > 1) {
      const defaultCurrency: CurrencyDetail = this._profileService.defaultCurrency;
      const baseCurrency: string = balance.hasOwnProperty(defaultCurrency.name) ? defaultCurrency.name : currencies[0];
      currencies.forEach(currency => {
        const currencyBalance: BudgetBalance = balance[currency];
        if (currency === baseCurrency) {
          value = value + currencyBalance.value;
          completeValue = completeValue + currencyBalance.completeValue;
        } else {
          value = value + this._currencyService.convertToCurrency(currencyBalance.value, currency, baseCurrency);
          completeValue = completeValue + this._currencyService.convertToCurrency(currencyBalance.completeValue, currency, baseCurrency);
        }
      });
    } else {
      value = balance[currencies[0]].value;
      completeValue = balance[currencies[0]].completeValue;
    }

    const percent: number = Math.round(value / completeValue * 100);
    return percent > 100 ? 100 : percent;
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
}
