import {Directive, ElementRef, EventEmitter, HostListener, Output} from '@angular/core';

@Directive({
  selector: '[bkClickOutside]'
})
export class ClickOutsideDirective {
  @Output()
  public clickOutside: EventEmitter<MouseEvent> = new EventEmitter<MouseEvent>();

  public constructor(private _elementRef: ElementRef) {}

  @HostListener('document:click', ['$event', '$event.target', '$event.currentTarget'])
  public onClick(event: MouseEvent, targetElement: HTMLElement, currentTarget: HTMLElement): void {
    if (!targetElement || !currentTarget.contains(targetElement)) {
      return;
    }

    if (!this._elementRef.nativeElement.contains(targetElement)) {
      this.clickOutside.emit(event);
    }
  }
}
