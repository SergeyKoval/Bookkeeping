import { Component, OnDestroy, OnInit } from '@angular/core';
import { Title } from '@angular/platform-browser';
import { ActivatedRoute, NavigationEnd, Router } from '@angular/router';

import { Subscription } from 'rxjs';
import { filter, map } from 'rxjs/internal/operators';

import { AlertService } from '../common/service/alert.service';
import { ProfileService } from '../common/service/profile.service';
import { LoadingService } from '../common/service/loading.service';
import { AuthenticationService } from '../common/service/authentication.service';
import { environment } from '../../environments/environment';

@Component({
  selector: 'bk-root',
  templateUrl: './bk.component.html',
  styleUrls: ['./bk.component.css']
})
export class BookkeepingRootComponent implements OnInit, OnDestroy {
  public authenticationCheckIndicator: boolean = false;
  public versionCheckIndicator: boolean = true;
  public versionError: boolean = false;
  public serverVersion: string;
  public uiVersion: string;

  private subscription: Subscription;
  private authenticationCheckSubscription: Subscription;

  public constructor(
    private _router: Router,
    private _activatedRoute: ActivatedRoute,
    private _alertService: AlertService,
    private _profileService: ProfileService,
    private _loadingService: LoadingService,
    private _titleService: Title,
    private _authenticationService: AuthenticationService
  ) {}

  public ngOnInit(): void {
    this._authenticationService.getServerVersion().subscribe(response => {
      if (response.message === environment.VERSION) {
        this.versionCheckIndicator = this.versionError = false;
      } else {
        this.versionCheckIndicator = false;
        this.versionError = true;
        this.serverVersion = response.message;
        this.uiVersion = environment.VERSION;
      }
    });

    this.authenticationCheckSubscription = this._loadingService.authenticationCheck$$.subscribe(value => {
      this.authenticationCheckIndicator = value;
    });

    this._router.events
      .pipe(
        filter(event => event instanceof NavigationEnd),
        map(value => {
          let child: ActivatedRoute = this._activatedRoute.firstChild;
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

  public isInitialDataLoaded(): boolean {
    return this._profileService.initialDataLoaded;
  }
}
