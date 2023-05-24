import { CurrencyConversion } from './currency-conversion';

export interface CurrencyHistory {
  name: string;
  year: number;
  month: number;
  day: number;
  conversions: CurrencyConversion;
}
