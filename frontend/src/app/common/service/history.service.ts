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

  public loadHistoryItems(page: number, pageLimit: number, showSms: boolean): Observable<HistoryType[]> {
    return this._http.post<HistoryType[]>('/api/history/page-portion', {'page': page, 'limit': pageLimit, 'withUnprocessedSms': showSms});
  }

  public addHistoryItem(historyItem: HistoryType, changeGoalStatus: boolean): Observable<SimpleResponse> {
    return this._http.post<SimpleResponse>('/api/history/add', {'historyItem': historyItem, 'changeGoalStatus': changeGoalStatus});
  }

  public editHistoryItem(historyItem: HistoryType, changeGoalStatus: boolean, changeOriginalGoalStatus: boolean): Observable<SimpleResponse> {
    return this._http.post<SimpleResponse>('/api/history/edit', {'historyItem': historyItem, 'changeGoalStatus': changeGoalStatus, 'changeOriginalGoalStatus': changeOriginalGoalStatus});
  }

  public deleteHistoryItem(historyItemId: string, changeGoalStatus: boolean): Observable<SimpleResponse> {
    return this._http.post<SimpleResponse>('/api/history/delete', {'id': historyItemId, 'changeGoalStatus': changeGoalStatus});
  }
}
