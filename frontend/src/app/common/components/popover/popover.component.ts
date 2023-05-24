import { Component, Input } from '@angular/core';
import { BrowserUtils } from '../../utils/browser-utils';
import { AvailbleBSPositions } from 'ngx-bootstrap/positioning';

@Component({
  selector: 'bk-popover',
  template: `<span [class]="getFinalClass()" [popover]="text" [placement]="placement">
<!--                   [popoverDisabled]="disabled" [popoverOnHover]="true" [popoverCloseOnMouseOutside]="true">-->
             </span>`,
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
  public placement: AvailbleBSPositions;
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
