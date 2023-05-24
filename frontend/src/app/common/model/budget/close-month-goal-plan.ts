import { BudgetBalance } from './budget-balance';

export interface CloseMonthGoalPlan {
  year: number;
  month: number;
  balance: BudgetBalance;
}
