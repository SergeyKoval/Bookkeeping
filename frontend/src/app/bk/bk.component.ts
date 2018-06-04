import { Component, OnDestroy, OnInit } from '@angular/core';
import { Title } from '@angular/platform-browser';
import { ActivatedRoute, NavigationEnd, Router } from '@angular/router';

import { Subscription } from 'rxjs';
import { filter, map } from 'rxjs/internal/operators';

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
    private _router: Router,
    private _activatedRoute: ActivatedRoute,
    private _alertService: AlertService,
    private _profileService: ProfileService,
    private _loadingService: LoadingService,
    private _titleService: Title
  ) {}

  public ngOnInit(): void {
    this.subscription = this._alertService.alerts.subscribe((alert: Alert) => {
      alert.initAutoClose(this.close.bind(this));
      this.alerts.push(alert);
    });

    this.authenticationCheckSubscription = this._loadingService.authenticationCheck$$.subscribe(value => {
      this.authenticationCheckIndicator = value;
    });

    this._router.events
      .pipe(
        filter(event => event instanceof NavigationEnd),
        map(value => {
          let child = this._activatedRoute.firstChild;
          while (child) {
            if (child.firstChild) {
              child = child.firstChild;
            } else if (child.snapshot.data && child.snapshot.data['title']) {
              return child.snapshot.data['title'];
            } else {
              return null;
            }
          }
          return null;
        })
      ).subscribe((title: string) => this._titleService.setTitle(title));
  }

  public ngOnDestroy(): void {
    this.subscription.unsubscribe();
  }

  public close(alertToClose: Alert): void {
    this.alerts = this.alerts.filter((alert: Alert) => alert !== alertToClose);
  }

  public isInitialDataLoaded(): boolean {
    return this._profileService.initialDataLoaded;
  }
}
