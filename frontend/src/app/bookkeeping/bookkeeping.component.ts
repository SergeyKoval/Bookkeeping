import { Component, OnDestroy, OnInit } from '@angular/core';

import { Subscription } from 'rxjs/Subscription';

import { AlertService } from '../common/service/alert.service';
import { Alert } from '../common/model/alert/Alert';
import { SettingsService } from '../common/service/settings.service';
import { AuthenticationService } from '../common/service/authentication.service';

@Component({
  selector: 'bk-bookkeeping',
  templateUrl: './bookkeeping.component.html',
  styleUrls: ['./bookkeeping.component.css']
})
export class BookkeepingComponent implements OnInit, OnDestroy {
  public alerts: Alert[] = [];
  private subscription: Subscription;

  public constructor(
    private _alertService: AlertService,
    private _settingsService: SettingsService,
    private _authenticationService: AuthenticationService
  ) {}

  public ngOnInit(): void {
    const ownerId: number = this._authenticationService.authenticatedProfile.id;
    this._settingsService.loadAccounts(ownerId);
    this._settingsService.loadCategories(ownerId);

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
