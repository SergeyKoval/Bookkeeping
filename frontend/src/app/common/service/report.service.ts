import { Inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';

import { Observable } from 'rxjs';
import { IMyDateRangeModel } from 'mydaterangepicker';

import { HOST } from '../config/config';
import { MultiLevelDropdownItem } from '../components/multi-level-dropdown/MultiLevelDropdownItem';
import { CheckboxState } from '../components/three-state-checkbox/CheckboxState';

@Injectable({
  providedIn: 'root'
})
export class ReportService {
  public constructor(
    private _http: HttpClient,
    @Inject(HOST) private _host: string
  ) { }

  public getHistoryItemsForPeriodReport(periodFilter: IMyDateRangeModel, operationsFilter: MultiLevelDropdownItem[], accountsFilter: MultiLevelDropdownItem[]): Observable<HistoryType[]> {
    const selectedOperations: string[][] = this.prepareFilteredItems(operationsFilter);
    const selectedAccounts: string[][] = this.prepareFilteredItems(accountsFilter);
    return this._http.post<HistoryType[]>('/api/report/history-actions', {
      'startPeriod': periodFilter.beginDate,
      'endPeriod': periodFilter.endDate,
      'operations': selectedOperations,
      'accounts': selectedAccounts
    });
  }

  private prepareFilteredItems(items: MultiLevelDropdownItem[]): string[][] {
    if (items.filter(operation => operation.state === CheckboxState.CHECKED).length === items.length) {
      return [];
    }

    const result: string[][] = [];
    items.forEach(item => {
      const hierarchies: string[][] = this.prepareItemHierarchy(item);
      if (hierarchies.length > 0) {
        result.push(...hierarchies);
      }
    });
    return result;
  }

  private prepareItemHierarchy(item: MultiLevelDropdownItem): string[][] {
    switch (item.state) {
      case CheckboxState.UNCHECKED:
        return [];
      case CheckboxState.CHECKED:
        return [[item.getAlias()]];
      case CheckboxState.INDETERMINATE:
        const result: string[][] = [];
        item.children.forEach(child => {
          const childHierarchies: string[][] = this.prepareItemHierarchy(child);
          if (childHierarchies.length > 0) {
            childHierarchies.forEach(hierarchies => {
              const deep: string[] = [item.getAlias()];
              deep.push(...hierarchies);
              result.push(deep);
            });
          }
        });
        return result;
    }
  }
}
