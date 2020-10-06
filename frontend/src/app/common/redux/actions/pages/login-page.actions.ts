import { createAction, props } from '@ngrx/store';

export const LOGIN_REDIRECT = createAction('[Authentication] Login Redirect', props<{ expiredSession: boolean }>());
export const LOGOUT = createAction('[Authentication] Logout');
export const SHOW_AUTHENTICATION_FORM = createAction('[Authentication] Show authentication form');
export const SHOW_REGISTRATION_FORM = createAction(
  '[Authentication] Show registration form',
  props<{ restorePasswordForm: boolean, message?: string, alwaysEditing?: boolean }>()
);
export const FORM_PRE_VALIDATION = createAction('[Authentication] Form pre validation');
export const NOTIFY_MISSED_USER = createAction('[Authentication] Retrieve notification about missed user', props<{ message: string }>());
export const SEND_CODE = createAction('[Authentication] Send code', props<{ registrationData: {} }>());
export const SEND_CODE_FINISHED = createAction('[Authentication] Send code finished', props<{ message: string }>());
export const ADD_ERROR_MESSAGE = createAction('[Authentication] Show error message', props<{ message: string }>());
export const REVIEW_CODE = createAction('[Authentication] Review code', props<{ registrationData: {} }>());
export const REVIEW_CODE_FINISHED = createAction('[Authentication] Review code finished', props<{ message: string }>());
export const AUTHENTICATE = createAction('[Authentication] Authenticate', props<{ credentials: { email: string, password: string } }>());
export const AUTHENTICATION_FINISHED = createAction('[Authentication] Authentication finished', props<{ message: string }>());
export const AUTHENTICATION_FAILED = createAction('[Authentication] Authentication failed', props<{ messageCode: string }>());
