import { Injectable } from '@angular/core';

import { Actions, createEffect, ofType } from '@ngrx/effects';
import { switchMap } from 'rxjs/operators';

import { SettingsProfilePageActions, UserActions } from '../../../actions';
import { ProfileService } from '../../../../service/profile.service';
import { AlertType } from '../../../reducers';

@Injectable()
export class SettingsProfilePageEffects {
  public handleProfile$ = createEffect(
    () => this._actions$.pipe(
      ofType(SettingsProfilePageActions.UPDATE_PASSWORD),
      switchMap(({ newPassword, oldPassword }) => this._profileService.updatePassword({ newPassword, oldPassword })),
      switchMap((response) => {
        if (response.status === 'FAIL') {
          return [
            UserActions.SHOW_ALERT({ alert: { type: AlertType.WARNING, message: response.message === 'INVALID_PASSWORD' ? 'Неверный старый пароль' : 'Ошибка сервера' } }),
            SettingsProfilePageActions.UPDATE_PASSWORD_FINISHED( { successResult: false })
          ];
        } else {
          return [
            UserActions.SHOW_ALERT({ alert: { type: AlertType.SUCCESS, message: 'Пороль успешно изменен' } }),
            SettingsProfilePageActions.UPDATE_PASSWORD_FINISHED( { successResult: true })
          ];
        }
      })
    )
  );

  public constructor (
    private _actions$: Actions,
    private _profileService: ProfileService,
  ) {}
}
