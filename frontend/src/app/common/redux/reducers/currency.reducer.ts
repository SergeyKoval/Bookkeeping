import { createFeatureSelector, createReducer, createSelector, on } from '@ngrx/store';

import { BkState } from './index';
import { LoginPageActions, UserActions } from '../actions';
import { DateUtils } from '../../utils/date-utils';

export const CURRENCIES_FEATURE_KEY = 'currencies';

export interface CurrencyHistory {
  name: string;
  year: number;
  month: number;
  day: number;
  conversions: CurrencyConversion;
}

export interface CurrencyConversion {
  [key: string]: number;
}

export interface CurrencyHistoryState extends BkState {
  // Map<String, CurrencyHistory>
  todayConversions: {};
  // Map<string, Map<number, Map<number, Array<CurrencyHistory>>>>
  currencies: {};
  // Map<string, Map<number, Map<number, boolean>>>
  currenciesIndicatorMap: {};
}

export const INITIAL_STATE: CurrencyHistoryState = {
  todayConversions: {},
  currencies: {},
  currenciesIndicatorMap: {}
};

export const REDUCER = createReducer(
  INITIAL_STATE,
  on(LoginPageActions.LOGIN_REDIRECT, () => ({
    ...INITIAL_STATE
  })),
  on(UserActions.LOAD_CURRENCIES_FOR_CURRENT_MONTH_FINISHED, (state, { currencyHistoryState }) => ({
    ...currencyHistoryState
  }))
);

export const selectCurrenciesState = createFeatureSelector<CurrencyHistoryState>(CURRENCIES_FEATURE_KEY);
export const selectCurrenciesIndicators = createSelector(selectCurrenciesState, state => state.currenciesIndicatorMap);
export const isCurrencyHistoryLoaded = createSelector(selectCurrenciesState, (state, {currency, date = DateUtils.getDateFromUTC()}) => {
  const currenciesIndicators = state.currenciesIndicatorMap;
  if (!currenciesIndicators[currency]) {
    return false;
  }

  const currencyIndicator = currenciesIndicators[currency];
  if (!currencyIndicator[date.year]) {
    return false;
  }

  const yearIndicator = currencyIndicator[date.year];
  return !!yearIndicator[date.month];
});
