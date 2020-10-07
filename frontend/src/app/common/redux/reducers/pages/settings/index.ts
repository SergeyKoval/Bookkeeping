import { Action, combineReducers, createFeatureSelector, createSelector } from '@ngrx/store';

import * as fromProfilePage from './profile-page.reducer';
import { BkState } from '../../index';
import { PROFILE_PAGE_FEATURE_KEY } from './profile-page.reducer';

export const SETTINGS_PAGE_FEATURE_KEY = 'settingsPage';

export { fromProfilePage };

export interface SettingsPageState {
  [PROFILE_PAGE_FEATURE_KEY]: fromProfilePage.SettingsProfilePageState;
}

export interface State extends BkState {
  [SETTINGS_PAGE_FEATURE_KEY]: SettingsPageState;
}

export function REDUCERS(state: SettingsPageState | undefined, action: Action) {
  return combineReducers({
    [fromProfilePage.PROFILE_PAGE_FEATURE_KEY]: fromProfilePage.REDUCER,
  })(state, action);
}

export const selectSettingsPageState = createFeatureSelector<State, SettingsPageState>(SETTINGS_PAGE_FEATURE_KEY);

export const selectProfilePageState = createSelector(selectSettingsPageState, state => state.profilePage);
export const selectProfileSubmitIndicator = createSelector(selectProfilePageState, state => state.submitIndicator);
export const selectProfileResetForm = createSelector(selectProfilePageState, state => state.resetForm);
