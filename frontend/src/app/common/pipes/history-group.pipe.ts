import { Pipe, PipeTransform } from '@angular/core';

import {HistoryGroup} from '../model/history/HistoryGroup';
import {DateUtilsService} from '../utils/date-utils.service';
import {HistoryItem} from '../model/history/HistoryItem';

@Pipe({
  name: 'historyGroup'
})
export class HistoryGroupPipe implements PipeTransform {
  public transform(items: HistoryType[]): HistoryGroup[] {
    const historyGroups: HistoryGroup[] = [];
    const historyGroupsMap: Map<string, HistoryGroup> = new Map();

    items.forEach((item: HistoryType) => {
      const itemDate: Date = new Date();
      itemDate.setTime(item.date);
      const itemDateString: string = DateUtilsService.convertDateToString(itemDate);
      if (!historyGroupsMap.has(itemDateString)) {
        historyGroupsMap.set(itemDateString, new HistoryGroup(item.date, itemDateString, DateUtilsService.getDayOfWeek(itemDate), []));
      }

      const historyGroup: HistoryGroup = historyGroupsMap.get(itemDateString);
      historyGroup.historyItems.push(new HistoryItem(item, item.id, item.order, item.type, item.category, item.subCategory, item.icon, item.description, item.goal, item.balance));
    });

    historyGroupsMap.forEach((historyGroup: HistoryGroup) => {
      historyGroups.push(historyGroup);
      historyGroup.historyItems.sort((firstItem: HistoryItem, secondItem: HistoryItem) => secondItem.order - firstItem.order);
    });
    historyGroups.sort((firstGroup: HistoryGroup, secondGroup: HistoryGroup) => secondGroup.date - firstGroup.date);
    return historyGroups;
  }
}