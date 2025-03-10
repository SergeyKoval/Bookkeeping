import { Component, Input } from '@angular/core';
import { BrowserUtils } from '../../utils/browser-utils';

@Component({
  selector: 'bk-popover',
  template: `<span [class]="getFinalClass()" [matTooltipPosition]="placement" [matTooltip]="text"></span>`,
  styleUrls: ['./popover.component.css']
})
export class PopoverComponent {
  @Input()
  public spanClass: string = '';
  @Input()
  public text: string;
  @Input()
  public glyphiconClass: string;
  @Input()
  public placement: 'above' | 'below' | 'left' | 'right' | 'before' | 'after';
  @Input()
  public conditionalClass: {[className: string]: boolean} = {};

  public disabled: boolean;
  public finalClass: string;

  public constructor() {
    this.disabled = BrowserUtils.isMobileOrTablet();
  }

  public getFinalClass(): string {
    return `${this.spanClass} ${this.glyphiconClass ? 'action glyphicon glyphicon-' + this.glyphiconClass : ''} ${this.processConditionalClasses()}`;
  }

  private processConditionalClasses(): string {
    let result: string = '';
    Object.keys(this.conditionalClass).forEach(className => {
      if (this.conditionalClass[className]) {
        result = `${className} `;
      }
    });

    return result;
  }
}
