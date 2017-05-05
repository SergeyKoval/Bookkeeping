import {BalanceItem} from './BalanceItem';

export class SubCategory {
  public constructor(
    public name: string,
    public order: number,
    public balance: BalanceItem[]
  ) {}
}
