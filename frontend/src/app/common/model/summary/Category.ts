import {SubCategory} from './SubCategory';

export class Category {
  public constructor(
    public name: string,
    public order: number,
    public opened: boolean,
    public subCategories: SubCategory[]
  ) {}
}
