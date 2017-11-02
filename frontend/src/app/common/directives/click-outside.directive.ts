import { Directive, ElementRef, EventEmitter, HostListener, Output } from '@angular/core';

@Directive({
  selector: '[bkClickOutside]'
})
export class ClickOutsideDirective {
  @Output()
  public clickOutside: EventEmitter<MouseEvent> = new EventEmitter<MouseEvent>();

  public constructor(private _elementRef: ElementRef) {}

  @HostListener('document:click', ['$event', '$event.path'])
  public onClick(event: MouseEvent, path: HTMLElement[]): void {
    if (!path.includes(this._elementRef.nativeElement)) {
      this.clickOutside.emit(event);
    }
  }
}
