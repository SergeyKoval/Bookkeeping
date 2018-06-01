type SimpleResponse = {
  status: string,
  message?: string
}

type Profile = {
  id: number,
  email: string,
  roles: string[],
  currencies: CurrencyDetail[],
  categories: Category[],
  accounts: FinAccount[]
};

type CurrencyDetail = {
  defaultCurrency: boolean,
  name: string,
  order: number,
  symbol: string,
};

type CurrencyHistory = {
  name: string,
  year: number,
  month: number,
  day: number,
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
  settingsOpened: boolean,
  subAccounts: SubAccount[]
};

type SubAccount = {
  title: string,
  order: number,
  icon: string,
  balance: {[currency: string]: number}
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
  subCategories: SubCategory[],
  opened: boolean
};

type SubCategory = {
  order: number,
  title: string,
  type: string
};
