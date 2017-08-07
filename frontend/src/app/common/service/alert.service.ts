import { Injectable } from '@angular/core';

import {Subject} from 'rxjs/Subject';
import {Observable} from 'rxjs/Observable';
import {Alert} from '../model/alert/Alert';
import {AlertType} from '../model/alert/AlertType';

@Injectable()
export class AlertService {
  private _alerts: Subject<Alert> = new Subject();

  public constructor() {}

  public get alerts(): Observable<Alert> {
    return this._alerts;
  }

  public addAlert(type: AlertType, message: string, title?: string, timeoutSeconds?: number): void {
    this.addAlertObject(new Alert(type, title, message, timeoutSeconds));
  }

  public addAlertObject(alert: Alert): void {
    this._alerts.next(alert);
  }
}
