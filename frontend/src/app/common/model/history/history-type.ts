import { HistoryBalanceType } from './history-balance-type';
import { Sms } from './sms';

export interface HistoryType {
  year: number;
  month: number;
  day: number;
  order?: number;
  type: string;
  category?: string;
  subCategory?: string;
  balance: HistoryBalanceType;
  goal?: string;
  description?: string;
  id?: string;
  archived: boolean;
  notProcessed?: boolean;
  sms?: Sms[];
}
