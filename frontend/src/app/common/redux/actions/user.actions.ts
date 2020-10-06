import { createAction, props } from '@ngrx/store';

import { Alert } from '../reducers';
import { Profile } from '../reducers/user/profile.reducer';
import { CurrencyHistoryState } from '../reducers/currency.reducer';

export const CHECK_AUTHORIZATION = createAction('[User] Check authorization');
export const START_AUTHENTICATION_EXPIRATION_JOB = createAction('[User] Start authentication expiration job');
export const LOAD_FULL_PROFILE = createAction('[User] Load full profile', props<{ redirectPage: Array<string> }>());
export const LOAD_PROFILE_FINISHED = createAction('[User] Load profile finished', props<{ profile: Profile }>());
export const LOAD_PROFILE_FAIL = createAction('[User] Load profile fail', props<{ message: string }>());
export const SHOW_ALERT = createAction('[Alert] Show message', props<{ alert: Alert }>());
export const CHANGE_PAGE = createAction('[Navigation] Change page', props<{ redirectPage: Array<string> }>());
export const CLOSE_DIALOGS = createAction('[User] Close all dialogs');

export const LOAD_CURRENCIES_FOR_CURRENT_MONTH = createAction(
  '[User] Load currencies for current month',
  props<{ profileCurrencies: Array<string>, redirectPage: Array<string> }>()
);
export const LOAD_CURRENCIES_FOR_CURRENT_MONTH_FINISHED = createAction(
  '[User] Load currencies for current month finished',
  props<{ currencyHistoryState: CurrencyHistoryState }>()
);
