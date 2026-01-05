import { CurrencyDetail } from './currency-detail';
import { Category } from './category';
import { FinAccount } from './fin-account';
import { Device } from './device';
import { Tag } from './tag';

export interface Profile {
  id: number;
  email: string;
  roles: string[];
  currencies: CurrencyDetail[];
  categories: Category[];
  accounts: FinAccount[];
  devices: {[deviceId: string]: Device};
  tags: Tag[];
}
