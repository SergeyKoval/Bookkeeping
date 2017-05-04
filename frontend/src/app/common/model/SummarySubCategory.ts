import {SummaryBalanceItem} from './SummaryBalanceItem';

export class SummarySubCategory {
  public constructor(
    public name: string,
    public order: number,
    public balance: SummaryBalanceItem[]
  ) {}
}
