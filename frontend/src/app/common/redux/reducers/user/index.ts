import { Action, combineReducers, createFeatureSelector, createReducer, createSelector, on } from '@ngrx/store';

import * as fromUserAccounts from './account.reducer';
import * as fromUserCurrencies from './currency.reducer';
import * as fromUserCategories from './category.reducer';
import * as fromUserProfile from './profile.reducer';
import { BkState } from '../index';
import { LoginPageActions, UserActions } from '../../actions';

export { fromUserCurrencies, fromUserProfile, fromUserCategories, fromUserAccounts };

export const USER_FEATURE_KEY = 'user';
export const USER_ROOT_FEATURE_KEY = 'userRoot';

export interface UserState {
  [USER_ROOT_FEATURE_KEY]: RootUserState;
  [fromUserProfile.PROFILE_FEATURE_KEY]: fromUserProfile.State;
  [fromUserCurrencies.CURRENCIES_FEATURE_KEY]: fromUserCurrencies.State;
  [fromUserCategories.CATEGORIES_FEATURE_KEY]: fromUserCategories.State;
  [fromUserAccounts.ACCOUNTS_FEATURE_KEY]: fromUserAccounts.State;
}

export interface RootUserState extends BkState {
  initialDataLoading: boolean;
  initialDataReady: boolean;
  permissionsChecking: boolean;
  accountsLoading: boolean;
}

export interface State extends BkState {
    [USER_FEATURE_KEY]: UserState;
}

export function REDUCERS(state: UserState | undefined, action: Action) {
  return combineReducers({
    [fromUserProfile.PROFILE_FEATURE_KEY]: fromUserProfile.REDUCER,
    [fromUserCurrencies.CURRENCIES_FEATURE_KEY]: fromUserCurrencies.REDUCER,
    [fromUserCategories.CATEGORIES_FEATURE_KEY]: fromUserCategories.REDUCER,
    [fromUserAccounts.ACCOUNTS_FEATURE_KEY]: fromUserAccounts.REDUCER,
    [USER_ROOT_FEATURE_KEY]: REDUCER
  })(state, action);
}

export const INITIAL_STATE: RootUserState = {
  initialDataLoading: false,
  initialDataReady: false,
  permissionsChecking: false,
  accountsLoading: false
};


export const REDUCER = createReducer(
  INITIAL_STATE,
  on(LoginPageActions.LOGIN_REDIRECT, () => ({
    ...INITIAL_STATE
  })),
  on(UserActions.CHECK_AUTHORIZATION, (state) => ({
    ...state,
    permissionsChecking: true
  })),
  on(UserActions.LOAD_CURRENCIES_FOR_CURRENT_MONTH, (state) => ({
    ...state,
    permissionsChecking: false
  })),
  on(UserActions.LOAD_FULL_PROFILE, (state) => ({
    ...state,
    initialDataLoading: true
  })),
  on(LoginPageActions.AUTHENTICATION_FAILED, (state) => ({
    ...state,
    initialDataLoading: false,
    initialDataReady: false
  })),
  on(UserActions.LOAD_CURRENCIES_FOR_CURRENT_MONTH_FINISHED, (state) => ({
    ...state,
    initialDataLoading: false,
    initialDataReady: true
  }))
);

export const selectUserState = createFeatureSelector<State, UserState>(USER_FEATURE_KEY);

export const selectUserRootState = createSelector(selectUserState, state => state.userRoot);
export const selectInitialDataLoading = createSelector(selectUserRootState, state => state.initialDataLoading);
export const selectInitialDataReady = createSelector(selectUserRootState, (state) => state.initialDataReady);
export const selectPermissionsChecking = createSelector(selectUserRootState, (state) => state.permissionsChecking);
export const selectAccountsLoading = createSelector(selectUserRootState, (state) => state.accountsLoading);

export const selectProfileState = createSelector(selectUserState, (state) => state.profile);
export const selectProfile = createSelector(selectProfileState, (state) => state.profile);
export const selectProfileEmail = createSelector(selectProfileState, (state) => state.profile ? state.profile.email : '');

export const selectAccountsState = createSelector(selectUserState, (state) => state.accounts);
export const selectAccounts = createSelector(selectAccountsState, fromUserAccounts.selectAllAccounts);

export const selectCurrenciesState = createSelector(selectUserState, (state) => state.currencies);
export const selectCurrencies = createSelector(selectCurrenciesState, fromUserCurrencies.selectAllCurrencies);
