import { Component, Input } from '@angular/core';

@Component({
  selector: 'bk-template-variable',
  templateUrl: './template-variable.component.html',
  styleUrls: ['./template-variable.component.css']
})
export class TemplateVariableComponent {
  @Input()
  public value: any;
}
