import {Inject, Injectable} from '@angular/core';
import {Http, Response} from '@angular/http';
import {FormBuilder, FormGroup, Validators} from '@angular/forms';

import {Observable} from 'rxjs/Observable';

import {HOST} from '../config/config';
import {CurrencyService} from './currency.service';

import 'rxjs/add/operator/map';

@Injectable()
export class HistoryService {

  public constructor(
    private _http: Http,
    @Inject(HOST) private _host: string,
    private _formBuilder: FormBuilder,
    private _currencyService: CurrencyService
  ) {}

  public loadHistoryItems(ownerId: number, page: number, limit: number): Observable<HistoryType[]> {
    return this._http.get(`${this._host}/history?ownerId=${ownerId}&_limit=${limit}&_page=${page}`)
      .delay(1500)
      .map((response: Response) => response.json());
  }

  public deleteHistoryItem(historyItem: HistoryType): Observable<Response> {
    return this._http.delete(`${this._host}/history/${historyItem.id}`).delay(1500);
  }

  public initHistoryForm(historyItem: HistoryType): FormGroup {
    const balance: HistoryBalanceType = historyItem.balance;
    return this._formBuilder.group({
      date: [historyItem.date, [Validators.required]],
      type: [historyItem.type || 'expense', [Validators.required]],
      balance: this._formBuilder.group({
        currency: [balance ? balance.currency : this._currencyService.defaultCurrency.name, [Validators.required]],
        value: [balance ? balance.value : '', [Validators.required, Validators.min(0.01)]]
      }),
      description: [historyItem.description || '']
    });
  }
}
