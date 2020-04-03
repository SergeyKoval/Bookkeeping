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
    public sms?: Sms[],
    public icon?: string,
    public additionalIcon?: string,
    public showSmsIndex?: number
  ) {}

  public cloneOriginalItem(): HistoryType {
    const clone: HistoryType = Object.assign({}, this.originalItem);
    clone.balance = Object.assign({}, this.originalItem.balance);
    clone.balance.alternativeCurrency = Object.assign({}, this.originalItem.balance.alternativeCurrency);
    return clone;
  }
}
