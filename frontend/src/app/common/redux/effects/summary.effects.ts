import { Injectable } from '@angular/core';

import { Actions, createEffect, ofType } from '@ngrx/effects';
import { tap } from 'rxjs/operators';

import { ProfileService } from '../../service/profile.service';
import { SummaryActions } from '../actions';

@Injectable()
export class SummaryEffects {
  public toggleAccount$ = createEffect(
    () => this._actions$.pipe(
      ofType(SummaryActions.TOGGLE_ACCOUNT),
      tap(({ account, opened }) => this._profileService.toggleAccount(account, opened))
    ),
    { dispatch: false }
  );


  public constructor (
    private _actions$: Actions,
    private _profileService: ProfileService
  ) {}
}
