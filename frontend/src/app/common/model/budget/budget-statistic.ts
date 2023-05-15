import { BudgetCategory } from './budget-category';

export interface BudgetStatistic {
  year: number;
  month: number;
  category: BudgetCategory;
  used: boolean;
}
