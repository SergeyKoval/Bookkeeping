import {Component, Input} from '@angular/core';

@Component({
  selector: 'bk-spinner',
  template: `<md-spinner *ngIf="display" [style.width.px]="size" [style.height.px]="size" [style.margin]="'auto'"></md-spinner>`
})
export class SpinnerComponent {
  @Input()
  public display: boolean = true;
  @Input()
  public size: number = 40;
}
