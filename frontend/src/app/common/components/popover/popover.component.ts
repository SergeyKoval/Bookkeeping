import { Component, Input } from '@angular/core';
import { BrowserUtils } from '../../utils/browser-utils';

@Component({
  selector: 'bk-popover',
  template: `<span [class]="getClass()" [popover]="text" [popoverPlacement]="placement" [popoverDisabled]="disabled"
                   [popoverOnHover]="true" [popoverCloseOnMouseOutside]="true">
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
  public placement: string;
  public disabled: boolean;

  public constructor() {
    this.disabled = BrowserUtils.isMobileOrTablet();
  }

  public getClass(): string {
    return `${this.spanClass}${this.glyphiconClass ? 'action glyphicon glyphicon-' + this.glyphiconClass : ''}`;
  }
}
