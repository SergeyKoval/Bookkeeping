import { createAction, props } from '@ngrx/store';

export const UPDATE_PASSWORD = createAction('[Settings] Update password', props<{ oldPassword: string, newPassword: string }>());
export const UPDATE_PASSWORD_FINISHED = createAction('[Settings] Update password finished', props<{ successResult: boolean }>());
