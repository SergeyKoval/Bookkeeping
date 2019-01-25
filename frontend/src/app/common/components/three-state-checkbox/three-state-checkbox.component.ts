import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';

import { CheckboxState } from './CheckboxState';

@Component({
  selector: 'bk-three-state-checkbox',
  template: `<input type="checkbox" [indeterminate]="getIndeterminateValue()" [checked]="getCheckedValue()" (click)="changeState()">`
})
export class ThreeStateCheckboxComponent implements OnInit {
  @Input()
  public state: CheckboxState = CheckboxState.UNCHECKED;
  @Output()
  public stateChanged: EventEmitter<CheckboxState> = new EventEmitter();

  public ngOnInit(): void {
  }

  public getCheckedValue(): boolean {
    return this.state === CheckboxState.CHECKED;
  }

  public getIndeterminateValue(): boolean {
    return this.state === CheckboxState.INDETERMINATE;
  }

  public changeState(): void {
    this.state = this.state !== CheckboxState.UNCHECKED ? CheckboxState.UNCHECKED : CheckboxState.CHECKED;
    this.stateChanged.next(this.state);
  }
}
