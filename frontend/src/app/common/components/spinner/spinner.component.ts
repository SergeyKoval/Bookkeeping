import { Component, Input } from '@angular/core';

@Component({
    selector: 'bk-spinner',
    template: `@if (display) {<mat-spinner [diameter]="size" [style.margin]="'auto'"></mat-spinner>}`,
    standalone: false
})
export class SpinnerComponent {
  @Input()
  public display: boolean = true;
  @Input()
  public size: number = 40;
}
