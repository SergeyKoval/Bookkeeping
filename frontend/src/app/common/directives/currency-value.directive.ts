import { Directive, ElementRef, HostListener, Input, OnInit, Optional } from '@angular/core';
import { NgControl } from '@angular/forms';

import { CurrencyValuePipe } from '../pipes/currency-value.pipe';
import { CurrencyUtils } from '../utils/currency-utils';

@Directive({
  selector: '[bkCurrencyValue]'
})
export class CurrencyValueDirective implements OnInit {
  @Input()
  public bkCurrencyValue: number;
  @Input()
  public skipDecimalZeros: boolean;

  private _element: HTMLInputElement;

  public constructor(
    @Optional() private _ngControl: NgControl,
    private _elementRef: ElementRef,
    private _currencyValuePipe: CurrencyValuePipe
  ) {
    this._element = this._elementRef.nativeElement;
  }

  public ngOnInit(): void {
    const value: string = this._element.value;
    if (value) {
      this._element.value = this._currencyValuePipe.transform(Number(value), this.bkCurrencyValue, this.skipDecimalZeros);
    }
  }

  @HostListener('blur', ['$event.target.value'])
  private onBlur(value: string): void {
    const numberValue: number = CurrencyUtils.convertValue(value);
    if (this._ngControl) {
      this._ngControl.control.patchValue(numberValue);
    }
    this._element.value = this._currencyValuePipe.transform(numberValue, this.bkCurrencyValue, this.skipDecimalZeros);
  }
}
