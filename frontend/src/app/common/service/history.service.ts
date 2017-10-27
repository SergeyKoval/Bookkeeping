import { Inject, Injectable } from '@angular/core';
import { Headers, Http, Response } from '@angular/http';

import { Observable } from 'rxjs/Observable';

import { HOST } from '../config/config';
import { GoalFilterType } from '../model/history/GoalFilterType';
import { AuthenticationService } from './authentication.service';

import 'rxjs/add/operator/map';
import 'rxjs/add/operator/first';
import 'rxjs/add/operator/switchMap';

@Injectable()
export class HistoryService {
  public constructor(
    private _http: Http,
    private _authenticationService: AuthenticationService,
    @Inject(HOST) private _host: string
  ) {}

  public loadHistoryItems(ownerId: number, page: number, limit: number): Observable<HistoryType[]> {
    return this._http.get(`${this._host}/history?_sort=date&_order=DESC&ownerId=${ownerId}&_limit=${limit}&_page=${page}`)
      .delay(1500)
      .map((response: Response) => response.json());
  }

  public addHistoryItem(historyItem: HistoryType): Observable<Response> {
    return this.getLastHistoryItemForDay(historyItem.date, historyItem.ownerId)
      .do((historyTypes: HistoryType[]) => {
        // TODO: order number will e set on backend
        const orderNumber: number = historyTypes.length > 0 ? historyTypes[0].order + 1 : 0;
        historyItem.order = orderNumber;
      }).switchMap(() => this._http.post(`${this._host}/history`, historyItem, {headers: new Headers({'Content-Type': 'application/json'})}))
      .delay(1500);
  }

  public deleteHistoryItem(historyItem: HistoryType): Observable<Response> {
    return this._http.delete(`${this._host}/history/${historyItem.id}`).delay(1500);
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

  private getLastHistoryItemForDay(date: number, ownerId: number): Observable<HistoryType[]> {
    return this._http.get(`${this._host}/history?_sort=order&_order=DESC&ownerId=${ownerId}&date=${date}&_limit=1&_page=1`)
      .map((response: Response) => response.json());
  }
}
