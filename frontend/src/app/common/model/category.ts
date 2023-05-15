import { SubCategory } from './sub-category';

export interface Category {
  id: number;
  ownerId: number;
  order: number;
  icon: string;
  title: string;
  subCategories: SubCategory[];
  opened: boolean;
}
