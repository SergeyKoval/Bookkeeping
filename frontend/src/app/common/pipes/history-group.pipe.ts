import { Pipe, PipeTransform } from '@angular/core';

import { HistoryGroup } from '../model/history/HistoryGroup';
import { DateUtils } from '../utils/date-utils';
import { HistoryItem } from '../model/history/HistoryItem';
import { ProfileService } from '../service/profile.service';

@Pipe({
  name: 'historyGroup'
})
export class HistoryGroupPipe implements PipeTransform {

  public constructor(private _authenticationService: ProfileService) {}

  public transform(items: HistoryType[]): HistoryGroup[] {
    const historyGroups: HistoryGroup[] = [];
    const historyGroupsMap: Map<string, HistoryGroup> = new Map();

    items.forEach((item: HistoryType) => {
      const itemDateString: string = DateUtils.convertDateToString(item.year, item.month, item.day);
      if (!historyGroupsMap.has(itemDateString)) {
        const date: Date = new Date(item.year, item.month - 1, item.day);
        historyGroupsMap.set(itemDateString, new HistoryGroup(date, itemDateString, DateUtils.getDayOfWeek(date), []));
      }
      const historyGroup: HistoryGroup = historyGroupsMap.get(itemDateString);
      const historyItem: HistoryItem = new HistoryItem(item, item.order, item.type, item.category, item.subCategory, item.description, item.balance, item.goal, item.archived);

      switch (item.type) {
        case 'expense':
        case 'income':
          historyItem.icon = this._authenticationService.getCategoryIcon(item.category);
          break;
        case 'transfer':
          historyItem.additionalIcon = this._authenticationService.getAccountIcon(item.balance.accountTo, item.balance.subAccountTo);
        case 'balance':
        case  'exchange':
          historyItem.icon = this._authenticationService.getAccountIcon(item.balance.account, item.balance.subAccount);
          break;
      }

      historyGroup.historyItems.push(historyItem);
    });

    historyGroupsMap.forEach((historyGroup: HistoryGroup) => {
      historyGroups.push(historyGroup);
      historyGroup.historyItems.sort((firstItem: HistoryItem, secondItem: HistoryItem) => secondItem.order - firstItem.order);
    });
    historyGroups.sort((firstGroup: HistoryGroup, secondGroup: HistoryGroup) => secondGroup.date.getTime() - firstGroup.date.getTime());
    return historyGroups;
  }
}
