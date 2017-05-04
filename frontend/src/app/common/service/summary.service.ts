import {Inject, Injectable} from '@angular/core';
import {Http, Response} from '@angular/http';

import {Subject} from 'rxjs/Subject';
import {Subscription} from 'rxjs/Subscription';
import {Observable} from 'rxjs/Observable';

import {HOST} from '../config/config';

@Injectable()
export class SummaryService {
  private _summaries$$: Subject<Summary[]> = new Subject();

  public constructor(
    private _http: Http,
    @Inject(HOST) private _host: string
  ) {}

  public loadSummaries(ownerId: number): void {
    const subscription: Subscription = this._http.get(`${this._host}/summaries?ownerId=${ownerId}`)
      .delay(1500)
      .subscribe((response: Response) => {
        const currencies: Summary[] = response.json();
        currencies.sort((first: Summary, second: Summary) => first.categoryOrder - second.categoryOrder);
        this._summaries$$.next(currencies);
        subscription.unsubscribe();
      });
  }

  public get summaries$(): Observable<Summary[]> {
    return this._summaries$$;
  }
}
