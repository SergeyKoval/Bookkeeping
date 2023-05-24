import { BudgetDetails } from './budget-details';

export interface Budget {
  id: string;
  year: number;
  month: number;
  expense: BudgetDetails;
  income: BudgetDetails;
}
