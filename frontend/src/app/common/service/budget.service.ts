import { Inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';

import { Observable } from 'rxjs';
import { tap } from 'rxjs/operators';

import { HOST } from '../config/config';
import { ProfileService } from './profile.service';
import { GoalFilterType } from '../model/history/GoalFilterType';
import { CurrencyService } from './currency.service';

@Injectable()
export class BudgetService {

  public constructor(
    private _http: HttpClient,
    @Inject(HOST) private _host: string,
    private _profileService: ProfileService,
    private _currencyService: CurrencyService
  ) { }

  public loadBudget(year: number, month: number): Observable<Budget> {
    return this._http.post<Budget>('/api/budget', {'year': year, 'month': month})
      .pipe(tap(budget => {
        this.clearEmptyCurrencies(budget.income);
        this.clearEmptyCurrencies(budget.expense);
      }));
  }

  public changeGoalDoneStatus(budgetId: string, type: string, category: string, goal: string, newDoneStatus: boolean): Observable<SimpleResponse> {
    return this._http.post<SimpleResponse>('/api/budget/changeGoalDoneStatus', {'budgetId': budgetId, 'type': type, 'category': category, 'goal': goal, 'doneStatus': newDoneStatus});
  }

  public toggleBudgetDetails(budgetId: string, type: string, opened: boolean): Observable<SimpleResponse> {
    return this._http.post<SimpleResponse>('/api/budget/toggleBudgetDetails', {'budgetId': budgetId, 'type': type, 'opened': opened});
  }

  public addBudgetCategory(budgetId: string, budgetType: string, categoryTitle: string, currencyBalance: BudgetBalance[]): Observable<SimpleResponse> {
    return this._http.post<SimpleResponse>('/api/budget/addBudgetCategory', {
      'budgetId': budgetId,
      'budgetType': budgetType,
      'categoryTitle': categoryTitle,
      'currencyBalance': currencyBalance
    });
  }

  public editBudgetCategory(budgetId: string, budgetType: string, categoryTitle: string, currencyBalance: BudgetBalance[]): Observable<SimpleResponse> {
    return this._http.post<SimpleResponse>('/api/budget/editBudgetCategory', {
      'budgetId': budgetId,
      'budgetType': budgetType,
      'categoryTitle': categoryTitle,
      'currencyBalance': currencyBalance
    });
  }

  public addBudgetGoal(budgetId: string, year: number, month: number, budgetType: string, categoryTitle: string, goalTitle: string, budgetBalance: BudgetBalance): Observable<SimpleResponse> {
    return this._http.post<SimpleResponse>('/api/budget/addBudgetGoal', {
      'budgetId': budgetId,
      'year': year,
      'month': month,
      'type': budgetType,
      'category': categoryTitle,
      'goal': goalTitle,
      'balance': budgetBalance
    });
  }

  public editBudgetGoal(budgetId: string, year: number, month: number, budgetType: string, categoryTitle: string, originalGoalTitle: string, goalTitle: string,
                        budgetBalance: BudgetBalance, changeGoalState: boolean): Observable<SimpleResponse> {
    return this._http.post<SimpleResponse>('/api/budget/editBudgetGoal', {
      'budgetId': budgetId,
      'year': year,
      'month': month,
      'type': budgetType,
      'category': categoryTitle,
      'goal': goalTitle,
      'originalGoal': originalGoalTitle,
      'balance': budgetBalance,
      'changeGoalState': changeGoalState
    });
  }

  public updateBudgetLimit(budgetId: string, budgetType: string, budgetBalance: BudgetBalance[]): Observable<SimpleResponse> {
    return this._http.post<SimpleResponse>('/api/budget/updateBudgetLimit', {
      'budgetId': budgetId,
      'type': budgetType,
      'balance': budgetBalance
    });
  }

  public removeGoal(budgetId: string, budgetType: string, categoryTitle: string, goalTitle: string): Observable<SimpleResponse> {
    return this._http.post<SimpleResponse>('/api/budget/removeGoal', {
      'budgetId': budgetId,
      'type': budgetType,
      'category': categoryTitle,
      'goal': goalTitle
    });
  }

  public removeCategory(budgetId: string, budgetType: string, categoryTitle: string): Observable<SimpleResponse> {
    return this._http.post<SimpleResponse>('/api/budget/removeCategory', {
      'budgetId': budgetId,
      'type': budgetType,
      'category': categoryTitle
    });
  }

  public moveGoal(budgetId: string, budgetType: string, categoryTitle: string, goalTitle: string, year: number, month: number, value: number): Observable<SimpleResponse> {
    return this._http.post<SimpleResponse>('/api/budget/moveGoal', {
      'budgetId': budgetId,
      'type': budgetType,
      'category': categoryTitle,
      'goal': goalTitle,
      'year': year,
      'month': month,
      'value': value
    });
  }

  public reviewBudgetGoalBeforeDelete(historyItemId: string, goal: string): Observable<SimpleResponse> {
    return this._http.get<SimpleResponse>(`/api/budget/reviewBeforeRemoveHistoryItem/${historyItemId}`);
  }

  public calculatePercentDone(balance: {[currency: string]: BudgetBalance}, value: number = 0, fullPercent: boolean = false): number {
    let completeValue: number = 0;
    const currencies: string[] = Object.keys(balance);

    if (currencies.length === 0) {
      return 0;
    }

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
      value = value + balance[currencies[0]].value;
      completeValue = balance[currencies[0]].completeValue;
    }

    if (completeValue === 0) {
      return 100;
    } else {
      const percent: number = Math.round(value / completeValue * 100);
      return percent > 100 && fullPercent === false ? 100 : percent;
    }
  }

  private clearEmptyCurrencies(budgetDetails: BudgetDetails): void {
    this.clearBalanceEmptyCurrencies(budgetDetails.balance);
    budgetDetails.categories.forEach(category => {
      this.clearBalanceEmptyCurrencies(category.balance);
    });
  }

  private clearBalanceEmptyCurrencies(balance: {[currency: string]: BudgetBalance}): void {
    Object.keys(balance).forEach(currency => {
      if (balance[currency].value === 0 && balance[currency].completeValue === 0) {
        delete balance[currency];
      }
    });
  }

  public static filterGoals(goals: BudgetGoal[], filterType: GoalFilterType): BudgetGoal[] {
    if (!goals) {
      return [];
    }

    switch (filterType) {
      case GoalFilterType.DONE: {
        return goals.filter((goal: BudgetGoal) => goal.done);
      }
      case GoalFilterType.NOT_DONE: {
        return goals.filter((goal: BudgetGoal) => !goal.done);
      }
      case GoalFilterType.ALL: {
        return goals;
      }
    }
  }

  public static sortGoals(goals: BudgetGoal[], selectedGoal?: BudgetGoal): BudgetGoal[] {
    return goals.sort((firstGoal: BudgetGoal, secondGoal: BudgetGoal) => {
      if (firstGoal === selectedGoal) {
        return -3;
      }
      if (secondGoal === selectedGoal) {
        return 3;
      }

      if ((firstGoal.done && !secondGoal.done) || (!firstGoal.done && secondGoal.done)) {
        return secondGoal.done ? -2 : 2;
      }

      const firstPercentDone: number = firstGoal.balance.value / firstGoal.balance.completeValue;
      const secondPercentDone: number = secondGoal.balance.value / secondGoal.balance.completeValue;
      return firstPercentDone === secondPercentDone ? 0 : (firstPercentDone < secondPercentDone ? -1 : 1);
    });
  }
}
