import { createAction, props } from '@ngrx/store';

export const CHECK_SERVER_VERSION = createAction('[Application] Check server version', props<{ frontendVersion: string }>());
export const CHECK_SERVER_VERSION_FINISHED = createAction('[Application] Check server version finished', props<{ backendVersion: string, versionError: boolean }>());
