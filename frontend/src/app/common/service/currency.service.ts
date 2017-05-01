import {Inject, Injectable} from '@angular/core';
import {Http, Response} from '@angular/http';

import {Observable} from 'rxjs/Observable';
import {Subject} from 'rxjs/Subject';
import {ReplaySubject} from 'rxjs/ReplaySubject';
import {Subscription} from 'rxjs/Subscription';

import {HOST} from '../config/config';

import 'rxjs/add/operator/delay';
import 'rxjs/add/operator/map';

@Injectable()
export class CurrencyService {
  private _currencies: Subject<Currency[]> = new ReplaySubject(1);

  public constructor(
    private _http: Http,
    @Inject(HOST) private _host: string
  ) {}

  public loadCurrencies(ownerId: number): void {
    const subscription: Subscription = this._http.get(`${this._host}/currencies?ownerId=${ownerId}`)
      .delay(1500)
      .subscribe((response: Response) => {
        const currencies: Currency[] = response.json();
        currencies.sort((first: Currency, second: Currency) => first.order - second.order);
        this._currencies.next(currencies);
        subscription.unsubscribe();
      });
  }

  public get currencies(): Observable<Currency[]> {
    return this._currencies.asObservable();
  }
}
