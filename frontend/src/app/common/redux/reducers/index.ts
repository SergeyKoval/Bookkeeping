import { ActionReducer, ActionReducerMap, MetaReducer } from '@ngrx/store';

import { environment } from '../../../../environments/environment';
import * as fromLoginPage from './pages/login-page.reducer';
import * as fromSettingsPage from './pages/settings';
import * as fromUser from './user';
import * as fromCurrencies from './currency.reducer';

export {
  fromUser,
  fromLoginPage,
  fromSettingsPage,
  fromCurrencies
};

export interface BkState {}

export enum AlertType {
  SUCCESS = 'success',
  INFO = 'info',
  WARNING = 'warning',
  DANGER = 'danger'
}

export interface Alert {
  type: AlertType;
  message: string;
  title?: string;
}

export const reducers: ActionReducerMap<BkState> = {
  [fromLoginPage.LOGIN_PAGE_FEATURE_KEY]: fromLoginPage.REDUCER,
  [fromSettingsPage.SETTINGS_PAGE_FEATURE_KEY]: fromSettingsPage.REDUCERS,
  [fromUser.USER_FEATURE_KEY]: fromUser.REDUCERS,
  [fromCurrencies.CURRENCIES_FEATURE_KEY]: fromCurrencies.REDUCER
};

// console.log all actions
export function logger(reducer: ActionReducer<BkState>): ActionReducer<BkState> {
  return (state, action) => {
    const result: BkState = reducer(state, action);
    console.groupCollapsed(action.type);
    console.log('prev state', state);
    console.log('action', action);
    console.log('next state', result);
    console.groupEnd();

    return result;
  };
}

export const metaReducers: MetaReducer<BkState>[] = !environment.production ? [logger] : [];
