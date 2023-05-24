import { Injectable } from '@angular/core';

declare var $: any; // tslint:disable-line:no-any

import { AlertType } from '../model/alert/AlertType';

@Injectable({
  providedIn: 'root'
})
export class AlertService {

  public constructor() {}

  public addAlert(type: AlertType, message: string, title?: string): void {
    $.notify({
      'title': title,
      'message': message
    }, {
      'z_index': 999,
      'offset': {'x': AlertService.calculateHorizontalOffset(), 'y': 55},
      'type': type
    });
  }

  private static calculateHorizontalOffset(): number {
    const viewWidth: number = window.innerWidth;
    if (viewWidth > 1200) {
      return (window.innerWidth - 1170) / 2 + 15;
    } else if (viewWidth > 992) {
      return (window.innerWidth - 970) / 2 + 15;
    } else {
      return (window.innerWidth - 750) / 2 + 15;
    }
  }
}
