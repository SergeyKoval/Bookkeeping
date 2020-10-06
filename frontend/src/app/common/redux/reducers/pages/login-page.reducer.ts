import { createFeatureSelector, createReducer, createSelector, on } from '@ngrx/store';

import { ApplicationActions, LoginPageActions } from '../../actions';
import { BkState } from '../index';
import { BrowserUtils } from '../../../utils/browser-utils';

export const LOGIN_PAGE_FEATURE_KEY = 'loginPage';
const ALWAYS_EDITING_BASED_ON_DEVICE = BrowserUtils.isMobileOrTablet();

export interface FormState {
  authenticationMode: boolean;
  formSubmitted: boolean;
  restorePasswordForm?: boolean;
  codeSent?: boolean;
  email?: string;
  password?: string;
}

export interface LoginPageState extends BkState {
  error: string | null;
  authenticationChecking: boolean;
  alwaysEditing: boolean;
  versionError: boolean;
  versionChecking: boolean;
  formState: FormState;
  serverVersion: string;
}

export const INITIAL_STATE: LoginPageState = {
  error: null,
  versionError: false,
  versionChecking: true,
  authenticationChecking: false,
  alwaysEditing: ALWAYS_EDITING_BASED_ON_DEVICE,
  serverVersion: null,
  formState: {
    authenticationMode: true,
    formSubmitted: false
  }
};

export const REDUCER = createReducer(
  INITIAL_STATE,
  on(LoginPageActions.LOGIN_REDIRECT, (state, { expiredSession }) => ({
    ...INITIAL_STATE,
    versionChecking: false,
    error: expiredSession ? 'Сессия устарела' : null
  })),
  on(ApplicationActions.CHECK_SERVER_VERSION, (state) => ({
    ...state,
    versionChecking: true,
  })),
  on(ApplicationActions.CHECK_SERVER_VERSION_FINISHED, (state, { versionError, backendVersion }) => ({
    ...state,
    versionError,
    versionChecking: false,
    serverVersion: backendVersion
  })),
  on(LoginPageActions.SHOW_REGISTRATION_FORM, (state, { restorePasswordForm, message, alwaysEditing }) => ({
    ...state,
    authenticationChecking: false,
    alwaysEditing: alwaysEditing || ALWAYS_EDITING_BASED_ON_DEVICE,
    error: message,
    formState: {
      ...state.formState,
      restorePasswordForm,
      formSubmitted: false,
      authenticationMode: false,
      codeSent: false
    }
  })),
  on(LoginPageActions.SHOW_AUTHENTICATION_FORM, (state) => ({
    ...state,
    error: null,
    alwaysEditing: ALWAYS_EDITING_BASED_ON_DEVICE,
    formState: {
      formSubmitted: false,
      authenticationMode: true
    }
  })),
  on(LoginPageActions.FORM_PRE_VALIDATION, (state) => ({
    ...state,
    error: null,
    formState: {
      ...state.formState,
      formSubmitted: true,
    }
  })),
  on(LoginPageActions.SEND_CODE, (state) => ({
    ...state,
    authenticationChecking: true,
    formState: {
      ...state.formState,
      codeSent: true,
    }
  })),
  on(LoginPageActions.NOTIFY_MISSED_USER, (state, { message }) => ({
    ...state,
    error: message,
    authenticationChecking: false,
    formState: {
      ...state.formState,
      codeSent: false,
    }
  })),
  on(LoginPageActions.SEND_CODE_FINISHED, LoginPageActions.REVIEW_CODE_FINISHED, LoginPageActions.AUTHENTICATION_FINISHED, (state, { message }) => ({
    ...state,
    error: message,
    authenticationChecking: false
  })),
  on(LoginPageActions.ADD_ERROR_MESSAGE, (state, { message }) => ({
    ...state,
    error: message
  })),
  on(LoginPageActions.AUTHENTICATE, (state, { credentials }) => ({
    ...state,
    formState: {
      ...state.formState,
      email: credentials.email,
      password: credentials.password
    }
  })),
  on(LoginPageActions.REVIEW_CODE, LoginPageActions.AUTHENTICATE, (state) => ({
    ...state,
    authenticationChecking: true
  }))
);

export const selectLoginPageState = createFeatureSelector<LoginPageState>(LOGIN_PAGE_FEATURE_KEY);
export const selectError = createSelector(selectLoginPageState, state => state.error);
export const selectAuthenticationChecking = createSelector(selectLoginPageState, state => state.authenticationChecking);
export const selectAlwaysEditing = createSelector(selectLoginPageState, state => state.alwaysEditing);
export const selectVersionError = createSelector(selectLoginPageState, state => state.versionError);
export const selectVersionChecking = createSelector(selectLoginPageState, state => state.versionChecking);
export const selectServerVersion = createSelector(selectLoginPageState, state => state.serverVersion);

export const selectFormState = createSelector(selectLoginPageState, state => state.formState);
export const selectMode = createSelector(selectFormState, state => state.authenticationMode);
export const selectFormSubmitted = createSelector(selectFormState, state => state.formSubmitted);
export const selectRestorePasswordForm = createSelector(selectFormState, state => state.restorePasswordForm);
export const selectCodeSent = createSelector(selectFormState, state => state.codeSent);
