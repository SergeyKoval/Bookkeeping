import { Injectable } from '@angular/core';

import { Actions, createEffect, ofType } from '@ngrx/effects';
import { catchError, switchMap } from 'rxjs/operators';
import { forkJoin, of } from 'rxjs';

import { CurrencyService } from '../../service/currency.service';
import { UserActions } from '../actions';
import { AlertType } from '../reducers';
import { CurrencyHistory, CurrencyHistoryState } from '../reducers/currency.reducer';

@Injectable()
export class CurrencyEffects {
  public loadCurrenciesForCurrentMonth$ = createEffect(
    () => this._actions$.pipe(
      ofType(UserActions.LOAD_CURRENCIES_FOR_CURRENT_MONTH),
      switchMap(({ profileCurrencies, redirectPage }) => {
        const currentDate: Date = new Date(Date.now());
        const initialData = {
          month: currentDate.getUTCMonth() + 1,
          year: currentDate.getUTCFullYear(),
          currencies: profileCurrencies
        };

        return forkJoin({
          initialData: of(initialData),
          monthCurrencies: this._currencyService.loadCurrenciesForMoth(initialData),
          redirectPage: of(redirectPage)
        });
      }),
      switchMap(({ initialData, redirectPage, monthCurrencies }) => {
        const currencyHistoryState: CurrencyHistoryState = {
          ...this.prepareCurrenciesAndCurrencyIndicatorMaps(monthCurrencies, initialData.year, initialData.month),
          todayConversions: this.prepareTodayConversions( initialData, monthCurrencies)
        };

        return [
          UserActions.START_AUTHENTICATION_EXPIRATION_JOB(),
          UserActions.LOAD_CURRENCIES_FOR_CURRENT_MONTH_FINISHED({ currencyHistoryState }),
          redirectPage && redirectPage.length > 0 ? UserActions.CHANGE_PAGE({ redirectPage }) : null
        ].filter(action => action !== null);
      }),
      catchError(() => of(UserActions.SHOW_ALERT({ alert: { type: AlertType.DANGER, message: 'Ошибка при загрузке истории курсов валют' }})))
    )
  );

  public constructor (
    private _actions$: Actions,
    private _currencyService: CurrencyService
  ) {}

  private prepareTodayConversions(initialData: {month: number, year: number, currencies: Array<string>}, monthCurrencies: Array<CurrencyHistory>) {
    const todayConversions = {};
    initialData.currencies
      .filter(currency => !todayConversions[currency])
      .forEach(currency => {
        const todayConversion: CurrencyHistory = monthCurrencies
          .filter(value => value.name === currency)
          .reduce((first: CurrencyHistory, second: CurrencyHistory) => first.day > second.day ? first : second);
        todayConversions[currency] = todayConversion;
      });

    return todayConversions;
  }

  private prepareCurrenciesAndCurrencyIndicatorMaps(currencyHistories: Array<CurrencyHistory>, year: number, month: number) {
    const currencies = {};
    const currenciesIndicatorMap = {};
    currencyHistories.forEach(currencyHistory => {
      let currencyYears = {};
      const currency = currencyHistory.name;
      if (currencies[currency]) {
        currencyYears = currencies[currency];
      } else {
        currencies[currency] = currencyYears;
      }
      let indicatorYears = {};
      if (currenciesIndicatorMap[currency]) {
        indicatorYears = currenciesIndicatorMap[currency];
      } else {
        currenciesIndicatorMap[currency] = indicatorYears;
      }

      let currencyMonths = {};
      if (currencyYears[year]) {
        currencyMonths = currencyYears[year];
      } else {
        currencyYears[year] = currencyMonths;
      }
      let indicatorMonths = {};
      if (indicatorYears[year]) {
        indicatorMonths = indicatorYears[year];
      } else {
        indicatorYears[year] = indicatorMonths;
      }

      currencyMonths[month] = currencyHistories;
      indicatorMonths[month] = true;
    });

    return { currencies, currenciesIndicatorMap };
  }
}
