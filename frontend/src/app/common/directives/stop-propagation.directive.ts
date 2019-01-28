import { Directive, HostListener } from '@angular/core';

@Directive({
  selector: '[bkStopPropagation]'
})
export class StopPropagationDirective {
  @HostListener('click', ['$event'])
  public onClick(event: MouseEvent): void {
    event.stopPropagation();
  }
}
