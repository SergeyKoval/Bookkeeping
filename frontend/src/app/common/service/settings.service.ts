import {Inject, Injectable} from '@angular/core';
import {Http, Response} from '@angular/http';

import {Subject} from 'rxjs/Subject';
import {Observable} from 'rxjs/Observable';
import {ReplaySubject} from 'rxjs/ReplaySubject';

import {HOST} from '../config/config';
import {LoadingService} from './loading.service';

import 'rxjs/add/operator/do';

@Injectable()
export class SettingsService {
  private _accounts$$: Subject<FinAccount[]> = new ReplaySubject(1);

  public constructor(
    private _http: Http,
    @Inject(HOST) private _host: string,
    private _loadingService: LoadingService,
  ) {}

  public loadAccounts(ownerId: number): void {
    this._loadingService.accounts$$.next(true);
    this._http.get(`${this._host}/accounts?ownerId=${ownerId}`)
      .delay(1500)
      .do(() => this._loadingService.accounts$$.next(false))
      .subscribe((response: Response) => this._accounts$$.next(response.json()));


    // const subscription: Subscription = this._http.get(`${this._host}/summaries?ownerId=${ownerId}`)
    //   .delay(1500)
    //   .map((response: Response) => response.json())
    //   .subscribe((summaries: Summary[]) => {
    //     const accountMap: Map<string, Account> = new Map();
    //
    //     summaries.forEach((summary: Summary) => {
    //       const accountName: string = summary.account;
    //       if (!accountMap.has(accountName)) {
    //         accountMap.set(accountName, new Account(accountName, summary.accountOrder, summary.opened, []));
    //       }
    //
    //       const account: Account = accountMap.get(accountName);
    //       const summarySubAccount: SubAccount = new SubAccount(summary.subAccount, summary.subAccountOrder, []);
    //       account.subAccounts.push(summarySubAccount);
    //       summary.balance.forEach((summaryBalance: SummaryBalance) => {
    //         summarySubAccount.balance.push(new BalanceItem(summaryBalance.currency, summaryBalance.value));
    //       });
    //     });
    //
    //     const categories: Account[] = [];
    //     accountMap.forEach((value: Account, key: string) => categories.push(value));
    //     this._accounts$$.next(categories);
    //     subscription.unsubscribe();
    //   });
  }

  public get accounts$(): Observable<FinAccount[]> {
    return this._accounts$$;
  }
}
