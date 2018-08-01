type Budget = {
  id: string,
  year: number,
  month: number,
  expense: BudgetDetails,
  income: BudgetDetails
};

type BudgetDetails = {
  opened: boolean,
  balance: {[currency: string]: BudgetBalance}
  categories: BudgetCategory[]
};

type BudgetCategory = {
  title: string,
  balance: {[currency: string]: BudgetBalance},
  goals: BudgetGoal[],
  opened: boolean
};

type BudgetBalance = {
  value?: number,
  completeValue?: number
  currency?: string,
};

type BudgetGoal = {
  done: boolean,
  title: string,
  balance: BudgetBalance
};

type MonthProgress = {
  currentMonth: boolean,
  monthPercent: number
};
