import {SummarySubCategory} from './SummarySubCategory';

export class SummaryCategory {
  public constructor(
    public name: string,
    public order: number,
    public subCategories: SummarySubCategory[]
  ) {}
}
