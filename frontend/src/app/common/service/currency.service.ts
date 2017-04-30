import {Inject, Injectable} from '@angular/core';
import {Http, Response} from '@angular/http';
import {ActivatedRouteSnapshot, Resolve, RouterStateSnapshot} from '@angular/router';

import {Observable} from 'rxjs/Observable';

import {HOST} from '../config/config';
import {LoadingService} from './loading.service';
import {AuthenticationService} from './authentication.service';

import 'rxjs/add/operator/delay';
import 'rxjs/add/operator/map';

@Injectable()
export class CurrencyService implements Resolve<Currency[]> {

  public constructor(
    private _as: AuthenticationService,
    private _loadingService: LoadingService,
    private _http: Http,
    @Inject(HOST) private _host: string
  ) {}

  public resolve(route: ActivatedRouteSnapshot, state: RouterStateSnapshot): Observable<Currency[]> {
    return this.getCurrencies(this._as.authenticatedProfile.id);
  }

  public getCurrencies(ownerId: number): Observable<Currency[]> {
    return this._http.get(`${this._host}/currencies?ownerId=${ownerId}`)
      .delay(1500)
      .map((response: Response) => {
        const currencies: Currency[] = response.json();
        currencies.sort((first: Currency, second: Currency) => first.order - second.order);
        return currencies;
      });
  }
}
