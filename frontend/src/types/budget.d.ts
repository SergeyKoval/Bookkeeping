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
  showConfirm?: boolean,
  confirmValue?: boolean,
  selectedValue?: boolean
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

type GoalDetails = {
  done: boolean,
  title: string,
  currency: string,
  value: number,
  completeValue: number,
  changeStatus: boolean,
  percent: number
};

type BudgetStatistic = {
  year: number,
  month: number,
  category: BudgetCategory,
  used: boolean
};

type CloseMonthGoalPlan = {
  year: number,
  month: number,
  balance: BudgetBalance
};
