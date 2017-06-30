import {Inject, Injectable} from '@angular/core';
import {Http, Response} from '@angular/http';

import {Subject} from 'rxjs/Subject';
import {Subscription} from 'rxjs/Subscription';
import {Observable} from 'rxjs/Observable';

import {HOST} from '../config/config';
import {Account} from '../model/summary/Account';
import {SubAccount} from '../model/summary/SubAccount';
import {BalanceItem} from '../model/summary/BalanceItem';

import 'rxjs/add/operator/map';

@Injectable()
export class SummaryService {
  private _accounts$$: Subject<Account[]> = new Subject();

  public constructor(
    private _http: Http,
    @Inject(HOST) private _host: string
  ) {}

  public loadSummaries(ownerId: number): void {
    const subscription: Subscription = this._http.get(`${this._host}/summaries?ownerId=${ownerId}`)
      .delay(1500)
      .map((response: Response) => response.json())
      .subscribe((summaries: Summary[]) => {
        const accountMap: Map<string, Account> = new Map();

        summaries.forEach((summary: Summary) => {
          const accountName: string = summary.account;
          if (!accountMap.has(accountName)) {
            accountMap.set(accountName, new Account(accountName, summary.accountOrder, summary.opened, []));
          }

          const account: Account = accountMap.get(accountName);
          const summarySubAccount: SubAccount = new SubAccount(summary.subAccount, summary.subAccountOrder, []);
          account.subAccounts.push(summarySubAccount);
          summary.balance.forEach((summaryBalance: SummaryBalance) => {
            summarySubAccount.balance.push(new BalanceItem(summaryBalance.currency, summaryBalance.value));
          });
        });

        const categories: Account[] = [];
        accountMap.forEach((value: Account, key: string) => categories.push(value));
        this._accounts$$.next(categories);
        subscription.unsubscribe();
      });
  }

  public get accounts$(): Observable<Account[]> {
    return this._accounts$$;
  }
}
