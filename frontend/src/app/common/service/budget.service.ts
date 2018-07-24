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
}
