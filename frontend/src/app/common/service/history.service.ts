import {Inject, Injectable} from '@angular/core';
import {Headers, Http, Response} from '@angular/http';
import {FormBuilder, FormGroup, Validators} from '@angular/forms';

import {Observable} from 'rxjs/Observable';

import {HOST} from '../config/config';
import {CurrencyService} from './currency.service';

import 'rxjs/add/operator/map';
import 'rxjs/add/operator/first';
import 'rxjs/add/operator/switchMap';

@Injectable()
export class HistoryService {
  public constructor(
    private _http: Http,
    @Inject(HOST) private _host: string,
    private _formBuilder: FormBuilder,
    private _currencyService: CurrencyService
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

  public initHistoryForm(historyItem: HistoryType): FormGroup {
    const balance: HistoryBalanceType = historyItem.balance;
    return this._formBuilder.group({
      date: [historyItem.date, [Validators.required]],
      type: [historyItem.type || 'expense', [Validators.required]],
      category: [historyItem.category],
      subCategory: [historyItem.subCategory],
      balance: this._formBuilder.group({
        currency: [balance ? balance.currency : this._currencyService.defaultCurrency.name, [Validators.required]],
        value: [balance ? balance.value : '', [Validators.required, Validators.min(0.01)]]
      }),
      description: [historyItem.description || '']
    });
  }

  private getLastHistoryItemForDay(date: number, ownerId: number): Observable<HistoryType[]> {
    return this._http.get(`${this._host}/history?_sort=order&_order=DESC&ownerId=${ownerId}&date=${date}&_limit=1&_page=1`)
      .map((response: Response) => response.json());
  }
}
