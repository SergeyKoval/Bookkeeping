import { BudgetBalance } from './budget-balance';
import { BudgetCategory } from './budget-category';

export interface BudgetDetails {
  opened: boolean;
  balance: {[currency: string]: BudgetBalance};
  categories: BudgetCategory[];
}
