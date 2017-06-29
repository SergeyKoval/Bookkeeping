import {Directive, ElementRef, OnInit} from '@angular/core';

@Directive({
  selector: '[bkFocus]'
})
export class FocusDirective implements OnInit {
  public constructor(private _element: ElementRef) { }

  public ngOnInit(): void {
    this._element.nativeElement.focus();
  }
}
