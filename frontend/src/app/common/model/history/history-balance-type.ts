export interface HistoryBalanceType {
  value?: number;
  newValue?: number;
  account?: string;
  accountTo?: string;
  subAccount?: string;
  subAccountTo?: string;
  currency?: string;
  newCurrency?: string;
  alternativeCurrency?: {[currncy: string]: number};
}
