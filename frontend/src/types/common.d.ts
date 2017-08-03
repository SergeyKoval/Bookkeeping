type Currency = {
  ownerId: number,
  default: boolean,
  name: string,
  order: number,
  symbol: string,
  conversions: CurrencyConversion
};

type CurrencyConversion = {
  [key: string]: number
};

type FinAccount = {
  id: number,
  ownerId: number,
  title: string,
  order: number,
  opened: boolean,
  subAccounts: SubAccount[]
};

type SubAccount = {
  title: string,
  order: number,
  icon: string,
  balance: BalanceItem[]
};

type BalanceItem = {
  currency: string,
  value: number
};

type Category = {
  id: number,
  ownerId: number,
  order: number,
  icon: string,
  title: string,
  subCategories: SubCategory[]
};

type SubCategory = {
  order: number,
  title: string,
};