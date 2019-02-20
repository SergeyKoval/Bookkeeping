import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';

import { MultiLevelDropdownItem } from '../MultiLevelDropdownItem';
import { CheckboxState } from '../../three-state-checkbox/CheckboxState';

@Component({
  selector: 'bk-multi-level-dropdown-level',
  templateUrl: './multi-level-dropdown-level.component.html',
  styleUrls: ['./multi-level-dropdown-level.component.css']
})
export class MultiLevelDropdownLevelComponent implements OnInit {
  @Input()
  public dataModel: MultiLevelDropdownItem[];
  @Input()
  public nextLevels: boolean = false;
  @Input()
  public stopPropagation: boolean;
  @Input()
  public checkAllButton: boolean = false;
  @Input()
  public alternativeSelection: boolean = false;
  @Output()
  public childStateChange: EventEmitter<MultiLevelDropdownItem> = new EventEmitter();

  public ngOnInit (): void {
    if (this.alternativeSelection) {
      const firstSelected: MultiLevelDropdownItem = this.dataModel.filter(item => item.state !== CheckboxState.UNCHECKED)[0];
      this.uncheckOther(firstSelected);
    }
  }

  public changeState(item: MultiLevelDropdownItem): void {
    const newState: CheckboxState = item.state !== CheckboxState.CHECKED ? CheckboxState.CHECKED : CheckboxState.UNCHECKED;
    this.changeCheckboxState(item, newState);
  }

  public onChildStateChange(item: MultiLevelDropdownItem): void {
    const parent: MultiLevelDropdownItem = this.dataModel.find(dataModelItem => dataModelItem.children && dataModelItem.children.indexOf(item) !== -1);
    parent.state = this.calculateState(parent);
    this.childStateChange.next(parent);

    if (parent.state !== CheckboxState.UNCHECKED) {
      this.uncheckOther(parent);
    }
  }

  public changeCheckboxState(item: MultiLevelDropdownItem, newState: CheckboxState): void {
    this.processDescendants(item, newState);
    this.childStateChange.next(item);

    if (item.state !== CheckboxState.UNCHECKED) {
      this.uncheckOther(item);
    }
  }

  public checkAll(): void {
    const newState: CheckboxState = this.isAllChecked() ? CheckboxState.UNCHECKED : CheckboxState.CHECKED;
    this.dataModel.forEach(item => {
      item.state = newState;
      this.processDescendants(item, newState);
    });
  }

  public isAllChecked(): boolean {
    return this.dataModel.filter(item => item.state === CheckboxState.CHECKED).length === this.dataModel.length;
  }

  private uncheckOther(item: MultiLevelDropdownItem): void {
    if (this.alternativeSelection && item !== null) {
      this.dataModel.forEach(levelItem => {
        if (levelItem !== item) {
          levelItem.state = CheckboxState.UNCHECKED;
          this.processDescendants(levelItem, CheckboxState.UNCHECKED);
        }
      });
    }
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
