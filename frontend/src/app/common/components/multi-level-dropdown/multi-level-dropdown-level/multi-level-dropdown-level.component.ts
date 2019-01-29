import { Component, EventEmitter, Input, Output } from '@angular/core';

import { MultiLevelDropdownItem } from '../MultiLevelDropdownItem';
import { CheckboxState } from '../../three-state-checkbox/CheckboxState';

@Component({
  selector: 'bk-multi-level-dropdown-level',
  templateUrl: './multi-level-dropdown-level.component.html',
  styleUrls: ['./multi-level-dropdown-level.component.css']
})
export class MultiLevelDropdownLevelComponent {
  @Input()
  public dataModel: MultiLevelDropdownItem[];
  @Input()
  public nextLevels: boolean = false;
  @Input()
  public stopPropagation: boolean;
  @Output()
  public childStateChange: EventEmitter<MultiLevelDropdownItem> = new EventEmitter();

  public changeState(item: MultiLevelDropdownItem): void {
    const newState: CheckboxState = item.state !== CheckboxState.CHECKED ? CheckboxState.CHECKED : CheckboxState.UNCHECKED;
    this.changeCheckboxState(item, newState);
  }

  public onChildStateChange(item: MultiLevelDropdownItem): void {
    const parent: MultiLevelDropdownItem = this.dataModel.find(dataModelItem => dataModelItem.children && dataModelItem.children.indexOf(item) !== -1);
    parent.state = this.calculateState(parent);
    this.childStateChange.next(parent);
  }

  public changeCheckboxState(item: MultiLevelDropdownItem, newState: CheckboxState): void {
    this.processDescendants(item, newState);
    this.childStateChange.next(item);
  }

  private processDescendants(item: MultiLevelDropdownItem, newState: CheckboxState): void {
    item.state = newState;
    if (item.children) {
      item.children.forEach(child => this.processDescendants(child, newState));
    }
  }

  private calculateState(parent: MultiLevelDropdownItem): CheckboxState {
    const levelCount: number = parent.children.length;
    const checkedCount: number = parent.children.filter(child => child.state === CheckboxState.CHECKED).length;
    if (checkedCount === levelCount) {
      return CheckboxState.CHECKED;
    }

    const uncheckedCount: number = parent.children.filter(child => child.state === CheckboxState.UNCHECKED).length;
    return uncheckedCount === levelCount ? CheckboxState.UNCHECKED : CheckboxState.INDETERMINATE;
  }
}
