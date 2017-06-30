import {BalanceItem} from './BalanceItem';

export class SubAccount {
  public constructor(
    public name: string,
    public order: number,
    public balance: BalanceItem[]
  ) {}
}
