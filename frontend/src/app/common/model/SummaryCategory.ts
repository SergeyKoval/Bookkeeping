import {SummarySubCategory} from './SummarySubCategory';

export class SummaryCategory {
  public constructor(
    public name: string,
    public order: number,
    public opened: boolean,
    public subCategories: SummarySubCategory[]
  ) {}
}
