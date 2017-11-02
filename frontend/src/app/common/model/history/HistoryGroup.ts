import { HistoryItem } from './HistoryItem';

export class HistoryGroup {
  public constructor(
    public date: number,
    public dateString: string,
    public dayOfWeek: string,
    public historyItems: HistoryItem[]
  ) {}
}
