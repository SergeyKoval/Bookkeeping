type Currency = {
  ownerId: number,
  name: string,
  order: number,
  conversions: CurrencyConversion
};

type CurrencyConversion = {
  [key: string]: number
};
