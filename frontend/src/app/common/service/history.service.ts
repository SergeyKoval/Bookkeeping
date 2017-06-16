import {Inject, Injectable} from '@angular/core';
import {Http, Response} from '@angular/http';

import {Observable} from 'rxjs/Observable';

import {HOST} from '../config/config';

import 'rxjs/add/operator/map';

@Injectable()
export class HistoryService {

  public constructor(
    private _http: Http,
    @Inject(HOST) private _host: string
  ) {}

  public loadHistoryItems(ownerId: number, page: number, limit: number): Observable<HistoryType[]> {
    return this._http.get(`${this._host}/history?ownerId=${ownerId}&_limit=${limit}&_page=${page}`)
      .delay(1500)
      .map((response: Response) => response.json());
  }

  public deleteHistoryItem(historyItem: HistoryType): Observable<Response> {
    return this._http.delete(`${this._host}/history/${historyItem.id}`).delay(1500);
  }
}
