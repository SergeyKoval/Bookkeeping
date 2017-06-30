import {SubAccount} from './SubAccount';

export class Account {
  public constructor(
    public name: string,
    public order: number,
    public opened: boolean,
    public subAccounts: SubAccount[]
  ) {}
}
