import { Inject, Injectable } from '@angular/core';
import { HttpClient, HttpHeaders, HttpResponse } from '@angular/common/http';

import { Observable } from 'rxjs';
import { delay, switchMap, tap } from 'rxjs/operators';

import { HOST } from '../config/config';
import { GoalFilterType } from '../model/history/GoalFilterType';
import { ProfileService } from './profile.service';

@Injectable()
export class HistoryService {
  public constructor(
    private _http: HttpClient,
    private _authenticationService: ProfileService,
    @Inject(HOST) private _host: string
  ) {}

  public loadHistoryItems(page: number, pageLimit: number): Observable<HistoryType[]> {
    return this._http.post<HistoryType[]>('/api/history/page-portion', {page: page, limit: pageLimit});
  }

  public addHistoryItem(historyItem: HistoryType): Observable<SimpleResponse> {
    return this._http.post<SimpleResponse>('/api/history/add', historyItem);
  }

  public editHistoryItem(historyItem: HistoryType): Observable<SimpleResponse> {
    return this._http.post<SimpleResponse>('/api/history/edit', historyItem);
  }

  public deleteHistoryItem(historyItemId: string): Observable<SimpleResponse> {
    return this._http.post<SimpleResponse>('/api/history/delete', {id: historyItemId});
  }





































  public chooseBudgetBalanceBasedOnCurrency(historyItem: HistoryType, budgetCategory: BudgetCategory): BudgetBalance {
    const historyItemCurrency: string = historyItem.balance.currency;
    let budgetBalances: BudgetBalance[] = budgetCategory.balance.filter((budgetBalance: BudgetBalance) => budgetBalance.currency === historyItemCurrency);
    if (budgetBalances.length === 1) {
      return budgetBalances[0];
    }

    const defaultCurrency: string = this._authenticationService.defaultCurrency.name;
    budgetBalances = budgetCategory.balance.filter((budgetBalance: BudgetBalance) => budgetBalance.currency === defaultCurrency);
    if (budgetBalances.length === 1) {
      return budgetBalances[0];
    }

    return budgetCategory.balance[0];
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

  private getLastHistoryItemForDay(date: number): Observable<HistoryType[]> {
    const url: string = `${this._host}/history?_sort=order&_order=DESC&date=${date}&_limit=1&_page=1`;
    return this._http.get<HistoryType[]>(url, {headers: new HttpHeaders({'Cache-Control': 'no-cache'})});
  }
}
