type Budget = {
  id: number,
  ownerId: number,
  year: number,
  month: number,
  type: string,
  balance: BudgetBalance[],
  budgetCategories: BudgetCategory[]
};

type BudgetCategory = {
  order: number,
  category: string,
  balance: BudgetBalance[],
  goals: BudgetGoal[]
};

type BudgetBalance = {
  currency: string,
  value: number,
  completeValue: number
};

type BudgetGoal = {
  name: string,
  done: boolean,
  balance: BudgetBalance
};
