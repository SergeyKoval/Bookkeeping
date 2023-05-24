import { HistoryBalanceType } from './history-balance-type';
import { HistoryType } from './history-type';
import { DeviceMessage } from './deviceMessage';

export class HistoryItem {
  public constructor(
    public originalItem: HistoryType,
    public order: number,
    public type: string,
    public category: string,
    public subCategory: string,
    public description: string,
    public balance: HistoryBalanceType,
    public goal?: string,
    public archived?: boolean,
    public notProcessed?: boolean,
    public deviceMessages?: DeviceMessage[],
    public icon?: string,
    public additionalIcon?: string,
    public showDeviceMessageIndex?: number
  ) {}

  public cloneOriginalItem(): HistoryType {
    const clone: HistoryType = Object.assign({}, this.originalItem);
    if (this.originalItem.balance) {
      clone.balance = Object.assign({}, this.originalItem.balance);
      clone.balance.alternativeCurrency = Object.assign({}, this.originalItem.balance.alternativeCurrency);
    } else {
      clone.balance = {alternativeCurrency: {}};
    }
    return clone;
  }
}
