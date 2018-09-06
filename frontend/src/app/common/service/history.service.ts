import { Inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';

import { Observable } from 'rxjs';

import { HOST } from '../config/config';
import { ProfileService } from './profile.service';

@Injectable()
export class HistoryService {
  public constructor(
    private _http: HttpClient,
    private _authenticationService: ProfileService,
    @Inject(HOST) private _host: string
  ) {}

  public loadHistoryItems(page: number, pageLimit: number): Observable<HistoryType[]> {
    return this._http.post<HistoryType[]>('/api/history/page-portion', {'page': page, 'limit': pageLimit});
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
}
