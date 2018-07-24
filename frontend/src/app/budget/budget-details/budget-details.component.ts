import { Component, Input, OnInit } from '@angular/core';

import { ProfileService } from '../../common/service/profile.service';
import { CurrencyService } from '../../common/service/currency.service';

@Component({
  selector: 'bk-budget-details',
  templateUrl: './budget-details.component.html',
  styleUrls: ['./budget-details.component.css']
})
export class BudgetDetailsComponent implements OnInit {
  @Input()
  public budgetDetails: BudgetDetails;
  @Input()
  public type: string;
  @Input()
  public monthProgress: MonthProgress;

  public hasGoals: boolean = false;
  public goalsCount: number = 0;
  public goalsDone: number = 0;

  public constructor(
    private _profileService: ProfileService,
    private _currencyService: CurrencyService
  ) {}

  public ngOnInit(): void {
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
}
