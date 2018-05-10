import { Component, OnDestroy, OnInit } from '@angular/core';

import { Subscription } from 'rxjs';
import { isNullOrUndefined } from "util";

import { Alert } from '../common/model/alert/Alert';
import { AlertService } from '../common/service/alert.service';
import { ProfileService } from '../common/service/profile.service';
import { LoadingService } from '../common/service/loading.service';

@Component({
  selector: 'bk-root',
  templateUrl: './bk.component.html',
  styleUrls: ['./bk.component.css']
})
export class BookkeepingRootComponent implements OnInit, OnDestroy {
  public alerts: Alert[] = [];
  public authenticationCheckIndicator: boolean = false;

  private subscription: Subscription;
  private authenticationCheckSubscription: Subscription;

  public constructor(
    private _alertService: AlertService,
    private _profileService: ProfileService,
    private _loadingService: LoadingService
  ) {}

  public ngOnInit(): void {
    this.subscription = this._alertService.alerts.subscribe((alert: Alert) => {
      alert.initAutoClose(this.close.bind(this));
      this.alerts.push(alert);
    });

    this.authenticationCheckSubscription = this._loadingService.authenticationCheck$$.subscribe(value => {
      this.authenticationCheckIndicator = value;
    });
  }

  public ngOnDestroy(): void {
    this.subscription.unsubscribe();
  }

  public close(alertToClose: Alert): void {
    this.alerts = this.alerts.filter((alert: Alert) => alert !== alertToClose);
  }

  public isAuthenticated(): boolean {
    return !isNullOrUndefined(this._profileService.authenticatedProfile);
  }
}
