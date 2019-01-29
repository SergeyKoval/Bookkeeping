import { Directive, HostListener, Input } from '@angular/core';

@Directive({
  selector: '[bkStopPropagation]'
})
export class StopPropagationDirective {
  @Input()
  public bkStopPropagation: boolean = true;

  @HostListener('click', ['$event'])
  public onClick(event: MouseEvent): void {
    if (this.bkStopPropagation) {
      event.stopPropagation();
    }
  }
}
