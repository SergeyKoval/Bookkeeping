import { createEntityAdapter, EntityAdapter, EntityState } from '@ngrx/entity';
import { createReducer, on } from '@ngrx/store';

import { LoginPageActions, UserActions } from '../../actions';

export const CURRENCIES_FEATURE_KEY = 'currencies';

export interface Currency {
  defaultCurrency?: boolean;
  name?: string;
  order?: number;
  symbol?: string;
}

export interface State extends EntityState<Currency> {
  defaultCurrency: Currency | null;
}

export const ADAPTER: EntityAdapter<Currency> = createEntityAdapter<Currency>({
  selectId: currency => currency.name,
  sortComparer: (first, second) => first.order - second.order
});

export const INITIAL_STATE: State = ADAPTER.getInitialState({
  defaultCurrency: null
});

export const REDUCER = createReducer(
  INITIAL_STATE,
  on(LoginPageActions.LOGIN_REDIRECT, () => ({
    ...INITIAL_STATE
  })),
  on(UserActions.LOAD_PROFILE_FINISHED, (state, { profile }) => {
    const defaultCurrency = profile.currencies.find(currency => !!currency.defaultCurrency);
    return ADAPTER.setAll(profile.currencies, { ...state, defaultCurrency });
  })
);

const {
  selectAll,
} = ADAPTER.getSelectors();

export const selectAllCurrencies = selectAll;
