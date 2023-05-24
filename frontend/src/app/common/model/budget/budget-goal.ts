import { BudgetBalance } from './budget-balance';

export interface BudgetGoal {
  done: boolean;
  title: string;
  balance: BudgetBalance;
}
