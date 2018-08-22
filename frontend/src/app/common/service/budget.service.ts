import { Inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';

import { Observable } from 'rxjs';

import { HOST } from '../config/config';

@Injectable()
export class BudgetService {

  public constructor(
    private _http: HttpClient,
    @Inject(HOST) private _host: string
  ) { }

  public loadBudget(year: number, month: number): Observable<Budget> {
    return this._http.post<Budget>('/api/budget', {'year': year, 'month': month});
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
}
