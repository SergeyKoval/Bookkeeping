import { Component, EventEmitter, Input, Output } from '@angular/core';

import { MultiLevelDropdownItem } from './MultiLevelDropdownItem';

@Component({
  selector: 'bk-multi-level-dropdown',
  templateUrl: './multi-level-dropdown.component.html',
  styleUrls: ['./multi-level-dropdown.component.css']
})
export class MultiLevelDropdownComponent {
  @Input()
  public buttonTitle: string;
  @Input()
  public dataModel: MultiLevelDropdownItem[];
  @Input()
  public stopPropagation: boolean;
  @Input()
  public checkAllButton: boolean = false;
  @Input()
  public alternativeSelection: boolean = false;
  @Output()
  public stateChange: EventEmitter<boolean> = new EventEmitter();

  public onChildStateChange(item: MultiLevelDropdownItem): void {
    this.stateChange.next(true);
  }
}
