import { Component, Input } from '@angular/core';

@Component({
    selector: 'bk-template-variable',
    templateUrl: './template-variable.component.html',
    styleUrls: ['./template-variable.component.css'],
    standalone: false
})
export class TemplateVariableComponent {
  @Input()
  public value: any;
  @Input()
  public toggleState: boolean;

  public changeToggleState() {
    this.toggleState = !this.toggleState;
  }

  public isToggleState(): boolean {
    return this.toggleState;
  }
}
