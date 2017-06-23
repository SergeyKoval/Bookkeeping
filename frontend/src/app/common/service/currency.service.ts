import {Inject, Injectable} from '@angular/core';
import {Http, Response} from '@angular/http';

import {Observable} from 'rxjs/Observable';
import {Subject} from 'rxjs/Subject';
import {ReplaySubject} from 'rxjs/ReplaySubject';
import {Subscription} from 'rxjs/Subscription';

import {HOST} from '../config/config';
import {BalanceItem} from '../model/summary/BalanceItem';

import 'rxjs/add/operator/delay';
import 'rxjs/add/operator/map';
import 'rxjs/add/operator/do';

@Injectable()
export class CurrencyService {
  private _currencies: Map<string, Currency>;
  private _currencies$$: Subject<Currency[]> = new ReplaySubject(1);

  public constructor(
    private _http: Http,
    @Inject(HOST) private _host: string
  ) {}

  public loadCurrencies(ownerId: number): void {
    const subscription: Subscription = this._http.get(`${this._host}/currencies?ownerId=${ownerId}`)
      .delay(1500)
      .map((response: Response) => response.json())
      .do((currencies: Currency[]) => {
        this._currencies = new Map();
        currencies.forEach((currency: Currency) => this._currencies.set(currency.name, currency));
      })
      .subscribe((currencies: Currency[]) => {
        currencies.sort((first: Currency, second: Currency) => first.order - second.order);
        this._currencies$$.next(currencies);
        subscription.unsubscribe();
      });
  }

  public getCurrencySymbol(currencyName: string): string {
    return this._currencies.get(currencyName).symbol;
  }

  public sortSummaryBalanceItems(items: BalanceItem[]): BalanceItem[] {
    items.sort((firstItem: BalanceItem, secondItem: BalanceItem) => {
      return this._currencies.get(firstItem.currency).order - this._currencies.get(secondItem.currency).order;
    });
    return items;
  }

  public get currencies$(): Observable<Currency[]> {
    return this._currencies$$.asObservable();
  }

  public get defaultCurrency(): Currency {
    let defaultCurrency: Currency = null;
    this._currencies.forEach((currency: Currency) => {
      if (currency.default) {
        defaultCurrency = currency;
        return;
      }
    });

    return defaultCurrency || this._currencies.values().next().value;
  }

  public static convertToCurrency(value: number, currentCurrency: string, convertedCurrency: Currency): number {
    if (convertedCurrency.name === currentCurrency) {
      return value;
    }

    return convertedCurrency.conversions[currentCurrency] * value;
  }
}
