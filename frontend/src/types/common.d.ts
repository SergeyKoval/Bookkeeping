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
  category: string,
  opened: true,
  subCategory: string,
  categoryOrder: number,
  subCategoryOrder: number,
  balance: SummaryBalance[]
};

type SummaryBalance = {
  currency: string,
  value: number
};
