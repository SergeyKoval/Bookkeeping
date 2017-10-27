import { Inject, Injectable } from '@angular/core';
import { Http, Response } from '@angular/http';

import { Observable } from 'rxjs/Observable';
import { Subject } from 'rxjs/Subject';
import { ReplaySubject } from 'rxjs/ReplaySubject';
import { Subscription } from 'rxjs/Subscription';
import { IMyDate } from 'mydatepicker';

import { HOST } from '../config/config';
import { AuthenticationService } from './authentication.service';
import { DateUtils } from '../utils/date-utils';

import 'rxjs/add/operator/delay';
import 'rxjs/add/operator/map';
import 'rxjs/add/operator/do';

@Injectable()
export class CurrencyService {
  private _todayConversions: Map<String, CurrencyHistory> = new Map();
  private _currencies: Map<string, Map<number, Map<number, CurrencyHistory[]>>> = new Map();
  private _currenciesIndicatorMap: Map<string, Map<number, Map<number, boolean>>> = new Map();
  private _currenciesUpdate$$: Subject<null> = new ReplaySubject(1);

  public constructor(
    private _http: Http,
    @Inject(HOST) private _host: string,
    private _authenticationService: AuthenticationService,
  ) {}

  public loadCurrencies(month: number, year: number, currencies: string[]): void {
    currencies.forEach((currency: string) => {
      const subscription: Subscription = this._http.get(`${this._host}/currencies?name=${currency}&year=${year}&month=${month}`)
        .delay(5500)
        .map((response: Response) => response.json())
        .subscribe((currencyHistories: CurrencyHistory[]) => {
          if (!this._todayConversions.has(currency)) {
            const todayConversions: CurrencyHistory = currencyHistories.reduce((first: CurrencyHistory, second: CurrencyHistory) => first.day > second.day ? first : second)[0];
            this._todayConversions.set(currency, todayConversions);
          }

          let currencyYears: Map<number, Map<number, CurrencyHistory[]>>;
          if (this._currencies.has(currency)) {
            currencyYears = this._currencies.get(currency);
          } else {
            currencyYears = new Map();
            this._currencies.set(currency, currencyYears);
          }
          let indicatorYears: Map<number, Map<number, boolean>>;
          if (this._currenciesIndicatorMap.has(currency)) {
            indicatorYears = this._currenciesIndicatorMap.get(currency);
          } else {
            indicatorYears = new Map();
            this._currenciesIndicatorMap.set(currency, indicatorYears);
          }

          let currencyMonths: Map<number, CurrencyHistory[]>;
          if (currencyYears.has(year)) {
            currencyMonths = currencyYears.get(year);
          } else {
            currencyMonths = new Map();
            currencyYears.set(year, currencyMonths);
          }
          let indicatorMonths: Map<number, boolean>;
          if (indicatorYears.has(year)) {
            indicatorMonths = indicatorYears.get(year);
          } else {
            indicatorMonths = new Map();
            indicatorYears.set(year, indicatorMonths);
          }

          currencyMonths.set(month, currencyHistories);
          indicatorMonths.set(month, true);

          this._currenciesUpdate$$.next();
          subscription.unsubscribe();
        });
    });
  }

  public sortSummaryBalanceItems(items: BalanceItem[]): BalanceItem[] {
    items.sort((firstItem: BalanceItem, secondItem: BalanceItem) => {
      const firstItemOrder: number = this._authenticationService.getCurrencyDetails(firstItem.currency).order;
      const secondItemOrder: number = this._authenticationService.getCurrencyDetails(secondItem.currency).order;
      return firstItemOrder - secondItemOrder;
    });
    return items;
  }

  public get currenciesUpdate$(): Observable<null> {
    return this._currenciesUpdate$$.asObservable();
  }

  public convertToCurrency(value: number, currentCurrency: string, convertedCurrency: CurrencyDetail, date: IMyDate = DateUtils.getDateFromUTC()): number {
    if (convertedCurrency.name === currentCurrency) {
      return value;
    }

    return this.getCurrencyHistory(currentCurrency, date).conversions[convertedCurrency.name] * value;
  }

  public getCurrencyHistory(currency: string, date: IMyDate = DateUtils.getDateFromUTC()): CurrencyHistory {
    if (!this.isCurrencyHistoryLoaded(currency, date)) {
      return this._todayConversions.get(currency);
    }

    const yearCurrencies: Map<number, CurrencyHistory[]> = this._currencies.get(currency).get(date.year);
    if (yearCurrencies && yearCurrencies.has(date.month)) {
      const monthCurrencies: CurrencyHistory[] = yearCurrencies.get(date.month);
      if (monthCurrencies && monthCurrencies.length > 0) {
        const currencyConversions: CurrencyHistory = monthCurrencies.filter((currencyHistory: CurrencyHistory) => currencyHistory.day === date.day)[0];
        if (currencyConversions) {
          return currencyConversions;
        }
      }
    }

    return this._todayConversions.get(currency);
  }

  public isCurrencyHistoryLoaded(currency: string, date: IMyDate = DateUtils.getDateFromUTC()): boolean {
    if (this._currenciesIndicatorMap.has(currency)) {
      const currencyIndicator: Map<number, Map<number, boolean>> = this._currenciesIndicatorMap.get(currency);
      if (currencyIndicator.has(date.year)) {
        const yearIndicator: Map<number, boolean> = currencyIndicator.get(date.year);
        if (yearIndicator.has(date.month)) {
          return true;
        }
      }
    }

    return false;
  }
}
