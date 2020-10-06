import { createReducer, on } from '@ngrx/store';

import { Currency } from './currency.reducer';
import { BkState } from '../index';
import { LoginPageActions, UserActions } from '../../actions';
import { Category } from './category.reducer';

export const PROFILE_FEATURE_KEY = 'profile';

export interface Profile {
  id: number;
  email: string;
  roles: string[];
  currencies: Array<Currency>;
  categories: Array<Category>;
  accounts: FinAccount[];
  devices: {[deviceId: string]: Device};
}

export interface State extends BkState {
  profile: Profile;
}

export const INITIAL_STATE: State = {
  profile: null
};

export const REDUCER = createReducer(
  INITIAL_STATE,
  on(LoginPageActions.LOGIN_REDIRECT, () => ({
    ...INITIAL_STATE
  })),
  on(UserActions.LOAD_PROFILE_FINISHED, (state, { profile }) => ({
    profile
  }))
);
