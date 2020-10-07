import { createReducer, on } from '@ngrx/store';

import { BkState } from '../../index';
import { SettingsProfilePageActions } from '../../../actions';

export const PROFILE_PAGE_FEATURE_KEY = 'profilePage';

export interface SettingsProfilePageState extends BkState {
  submitIndicator: boolean;
  resetForm: boolean;
}

export const INITIAL_STATE: SettingsProfilePageState = {
  submitIndicator: false,
  resetForm: false
};

export const REDUCER = createReducer(
  INITIAL_STATE,
  on(SettingsProfilePageActions.UPDATE_PASSWORD, () => ({
    resetForm: false,
    submitIndicator: true
  })),
  on(SettingsProfilePageActions.UPDATE_PASSWORD_FINISHED, (state, { successResult }) => ({
    resetForm: successResult,
    submitIndicator: false
  }))
);
