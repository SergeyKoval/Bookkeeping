import {Directive, ElementRef, HostListener, OnInit} from '@angular/core';

import {CurrencyValuePipe} from '../pipes/currency-value.pipe';
import {NgControl} from '@angular/forms';

@Directive({
  selector: '[bkCurrencyValue]'
})
export class CurrencyValueDirective implements OnInit {
  private _element: HTMLInputElement;

  public constructor(
    private _ngControl: NgControl,
    private _elementRef: ElementRef,
    private _currencyValuePipe: CurrencyValuePipe
  ) {
    this._element = this._elementRef.nativeElement;
  }

  public ngOnInit(): void {
    const value: string = this._element.value;
    if (value) {
      this._element.value = this._currencyValuePipe.transform(Number(value), true);
    }
  }

  @HostListener('blur', ['$event.target.value'])
  private onBlur(value: string): void {
    value = value.replace('\'', '').replace(',', '.');
    if (value.length > 0) {
      const numberValue: number = Number(value);
      this._ngControl.control.patchValue(numberValue);
      this._element.value = this._currencyValuePipe.transform(numberValue, true);
    }
  }
}
