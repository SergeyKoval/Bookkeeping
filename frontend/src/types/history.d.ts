type HistoryType = {
  id: number,
  ownerId: number,
  date: number,
  order: number,
  type: string,
  category: string,
  subCategory: string,
  icon: string,
  balance: HistoryBalanceType,
  goal?: HistoryGoal,
  description?: string
};

type HistoryBalanceType = {
  value: number,
  category: string,
  subCategory: string,
  currency: string,
  alternativeCurrency: {[key: string]: number}
};

type HistoryGoal = {
  category: string,
  name: string
};
