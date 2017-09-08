import { Inject, Injectable } from '@angular/core';
import { Http, Response } from '@angular/http';

import { Observable } from 'rxjs/Observable';

import { HOST } from '../config/config';

@Injectable()
export class BudgetService {

  public constructor(
    private _http: Http,
    @Inject(HOST) private _host: string
  ) { }

  public loadBudgetItem(ownerId: number, year: number, month: number, category: string, type: string): Observable<BudgetItem[]> {
    return this._http.get(`${this._host}/budget?ownerId=${ownerId}&year=${year}&month=${month}&category=${category}&type=${type}`)
      .delay(1500)
      .map((response: Response) => response.json());
  }
}
