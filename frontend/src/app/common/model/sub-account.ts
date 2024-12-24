export interface SubAccount {
  title: string;
  order: number;
  icon: string;
  balance: {[currency: string]: number};
  excludeFromTotals: boolean;
}
