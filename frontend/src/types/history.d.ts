type HistoryType = {
  id?: number,
  ownerId: number,
  date: number,
  order?: number,
  type: string,
  category?: string,
  subCategory?: string,
  balance: HistoryBalanceType,
  goal?: string,
  description?: string
};

type HistoryBalanceType = {
  value: number,
  newValue?: number,
  account?: string,
  accountTo?: string,
  subAccount?: string,
  subAccountTo?: string,
  currency: string,
  newCurrency?: string,
  alternativeCurrency?: {[key: string]: number}
};
