import { Pipe, PipeTransform } from '@angular/core';

import { HistoryGroup } from '../model/history/HistoryGroup';
import { DateUtils } from '../utils/date-utils';
import { HistoryItem } from '../model/history/HistoryItem';
import { SettingsService } from '../service/settings.service';

@Pipe({
  name: 'historyGroup'
})
export class HistoryGroupPipe implements PipeTransform {

  public constructor(private _settingsService: SettingsService) {}

  public transform(items: HistoryType[]): HistoryGroup[] {
    const historyGroups: HistoryGroup[] = [];
    const historyGroupsMap: Map<string, HistoryGroup> = new Map();

    items.forEach((item: HistoryType) => {
      const itemDate: Date = new Date();
      itemDate.setTime(item.date);
      const itemDateString: string = DateUtils.convertDateToString(itemDate);
      if (!historyGroupsMap.has(itemDateString)) {
        historyGroupsMap.set(itemDateString, new HistoryGroup(item.date, itemDateString, DateUtils.getDayOfWeek(itemDate), []));
      }
      const historyGroup: HistoryGroup = historyGroupsMap.get(itemDateString);
      const historyItem: HistoryItem = new HistoryItem(item, item.id, item.order, item.type, item.category, item.subCategory, item.description, item.goal, item.balance);

      switch (item.type) {
        case 'expense':
        case 'income':
          historyItem.icon = this._settingsService.getCategoryIcon(item.category);
          break;
        case 'transfer':
          historyItem.additionalIcon = this._settingsService.getAccountIcon(item.balance.accountTo, item.balance.subAccountTo);
        case  'exchange':
          historyItem.icon = this._settingsService.getAccountIcon(item.balance.account, item.balance.subAccount);
          break;
      }

      historyGroup.historyItems.push(historyItem);
    });

    historyGroupsMap.forEach((historyGroup: HistoryGroup) => {
      historyGroups.push(historyGroup);
      historyGroup.historyItems.sort((firstItem: HistoryItem, secondItem: HistoryItem) => secondItem.order - firstItem.order);
    });
    historyGroups.sort((firstGroup: HistoryGroup, secondGroup: HistoryGroup) => secondGroup.date - firstGroup.date);
    return historyGroups;
  }
}
