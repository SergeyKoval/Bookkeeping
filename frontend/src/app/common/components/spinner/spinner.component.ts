import { Component, Input } from '@angular/core';

@Component({
  selector: 'bk-spinner',
  template: `<mat-spinner *ngIf="display" [diameter]="size" [style.margin]="'auto'"></mat-spinner>`
})
export class SpinnerComponent {
  @Input()
  public display: boolean = true;
  @Input()
  public size: number = 40;
}
