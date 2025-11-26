import { Directive, DoCheck, ElementRef, HostListener, Input, Optional } from '@angular/core';
import { NgControl } from '@angular/forms';

import { CurrencyValuePipe } from '../pipes/currency-value.pipe';
import { CurrencyUtils } from '../utils/currency-utils';

@Directive({
    selector: '[bkCurrencyValue]',
    standalone: false
})
export class CurrencyValueDirective implements DoCheck {
  @Input()
  public bkCurrencyValue: number;
  @Input()
  public skipDecimalZeros: boolean;
  @Input()
  public patchModelValueToFixedSize: boolean;

  private _ELEMENT: HTMLInputElement;
  private _INITIAL_LOAD_DONE: boolean = false;

  public constructor(
    @Optional() private _ngControl: NgControl,
    private _elementRef: ElementRef,
    private _currencyValuePipe: CurrencyValuePipe
  ) {
    this._ELEMENT = this._elementRef.nativeElement;
  }

  public ngDoCheck(): void {
    let value: string = this._ELEMENT.value;
    if (CurrencyUtils.ILLEGAL_CALCULATION_SYMBOLS_PATTERN.test(value)) {
      value = this._ELEMENT.value = value.substring(0, value.length - 1);
    }

    if (value && !this._INITIAL_LOAD_DONE) {
      this._ELEMENT.value = this._currencyValuePipe.transform(Number(value), this.bkCurrencyValue, this.skipDecimalZeros);
      this._INITIAL_LOAD_DONE = true;
    }
  }

  @HostListener('blur', ['$event.target.value'])
  protected onBlur(value: string): void {
    this._INITIAL_LOAD_DONE = true;
    const numberValue: number = CurrencyUtils.convertValue(value);
    if (this._ngControl) {
      const ngControlValue: number = this.patchModelValueToFixedSize ? Number(numberValue.toFixed(this.bkCurrencyValue)) : numberValue;
      this._ngControl.control.patchValue(ngControlValue);
    }
    this._ELEMENT.value = this._currencyValuePipe.transform(numberValue, this.bkCurrencyValue, this.skipDecimalZeros);
  }
}
