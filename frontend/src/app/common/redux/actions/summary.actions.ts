import { createAction, props } from '@ngrx/store';

export const TOGGLE_ACCOUNT = createAction('[Summary] toggle account', props<{ account: string, opened: boolean }>());
