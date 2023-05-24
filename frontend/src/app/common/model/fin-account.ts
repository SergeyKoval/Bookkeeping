import { SubAccount } from './sub-account';

export interface FinAccount {
  id: number;
  ownerId: number;
  title: string;
  order: number;
  opened: boolean;
  settingsOpened: boolean;
  subAccounts: SubAccount[];
}
