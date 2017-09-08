type BudgetItem = {
  id: number,
  ownerId: number,
  order: number,
  year: number,
  month: number,
  category: string,
  type: string,
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
