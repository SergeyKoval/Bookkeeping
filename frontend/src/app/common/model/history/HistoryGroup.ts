import { HistoryItem } from './HistoryItem';

export class HistoryGroup {
  public constructor(
    public date: Date,
    public dateString: string,
    public dayOfWeek: string,
    public historyItems: HistoryItem[]
  ) {}
}
