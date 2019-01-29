import { Component, EventEmitter, Input, Output } from '@angular/core';

import { CheckboxState } from './CheckboxState';

@Component({
  selector: 'bk-three-state-checkbox',
  template: `<input type="checkbox" [indeterminate]="getIndeterminateValue()" [checked]="getCheckedValue()" (click)="changeState()">`
})
export class ThreeStateCheckboxComponent {
  @Input()
  public state: CheckboxState = CheckboxState.UNCHECKED;
  @Output()
  public stateChanged: EventEmitter<CheckboxState> = new EventEmitter();

  public getCheckedValue(): boolean {
    return this.state === CheckboxState.CHECKED;
  }

  public getIndeterminateValue(): boolean {
    return this.state === CheckboxState.INDETERMINATE;
  }

  public changeState(): void {
    this.state = this.state !== CheckboxState.CHECKED ? CheckboxState.CHECKED : CheckboxState.UNCHECKED;
    this.stateChanged.next(this.state);
  }
}
