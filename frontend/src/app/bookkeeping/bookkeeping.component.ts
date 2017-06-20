import {Component, OnDestroy, OnInit} from '@angular/core';

import {Subscription} from 'rxjs/Subscription';

import {AlertService} from '../common/service/alert.service';
import {Alert} from '../common/model/alert/Alert';

@Component({
  selector: 'bk-bookkeeping',
  templateUrl: './bookkeeping.component.html',
  styleUrls: ['./bookkeeping.component.css']
})
export class BookkeepingComponent implements OnInit, OnDestroy {
  public alerts: Alert[] = [];
  private subscription: Subscription;

  public constructor(private _alertService: AlertService) {}

  public ngOnInit(): void {
    this.subscription = this._alertService.alerts.subscribe((alert: Alert) => {
      alert.initAutoClose(this.close.bind(this));
      this.alerts.push(alert);
    });
  }

  public ngOnDestroy(): void {
    this.subscription.unsubscribe();
  }

  public close(alertToClose: Alert): void {
    this.alerts = this.alerts.filter((alert: Alert) => alert !== alertToClose);
  }
}
