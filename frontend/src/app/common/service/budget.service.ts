import { Inject, Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';

import { Observable } from 'rxjs/Observable';
import { delay, map } from 'rxjs/operators';

import { HOST } from '../config/config';

@Injectable()
export class BudgetService {

  public constructor(
    private _http: HttpClient,
    @Inject(HOST) private _host: string
  ) { }

  public loadBudget(ownerId: number, year: number, month: number, type: string): Observable<Budget> {
    return this._http.get(`${this._host}/budget?ownerId=${ownerId}&year=${year}&month=${month}&type=${type}`, {headers: new HttpHeaders({'Cache-Control': 'no-cache'})})
      .pipe(
        delay(1500),
        map((response: Budget[]) => response[0])
      );
  }
}
