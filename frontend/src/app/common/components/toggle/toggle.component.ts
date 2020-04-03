import { Component, ElementRef, Input, Output, ViewChild } from '@angular/core';

import { Subject } from 'rxjs';

@Component({
  selector: 'bk-toggle',
  templateUrl: './toggle.component.html',
  styleUrls: ['./toggle.component.css']
})
export class ToggleComponent {
  @Input()
  public onValue: string;
  @Input()
  public onBadgeValue: string;
  @Input()
  public offValue: string;
  @Input()
  public offBadgeValue: string;
  @Input()
  public value: boolean = false;
  @Input()
  public width: number;
  @Output()
  public onValueChanged: Subject<boolean> = new Subject();

  @ViewChild ('inputElement', {static : true})
  private _INPUT_ELEMENT_REF: ElementRef;
  @ViewChild ('groupElement', {static : true})
  private _GROUP_ELEMENT_REF: ElementRef;

  public changeValue(): void {
    this.value = !this.value;
    this.onValueChanged.next(this.value);
  }
}
