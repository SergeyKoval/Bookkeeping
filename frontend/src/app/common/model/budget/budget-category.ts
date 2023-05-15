import { BudgetBalance } from './budget-balance';
import { BudgetGoal } from './budget-goal';

export interface BudgetCategory {
  title: string;
  balance: {[currency: string]: BudgetBalance};
  goals: BudgetGoal[];
  opened: boolean;
}
