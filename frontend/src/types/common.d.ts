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

type Summary = {
  ownerId: number,
  account: string,
  opened: true,
  subAccount: string,
  accountOrder: number,
  subAccountOrder: number,
  balance: SummaryBalance[]
};

type SummaryBalance = {
  currency: string,
  value: number
};
