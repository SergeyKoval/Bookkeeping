import { Component, ElementRef, EventEmitter, Input, OnInit, Output, ViewChild } from '@angular/core';
import { MatSlideToggleChange } from '@angular/material';

import { CurrencyUtils } from '../../utils/currency-utils';
import { CurrencyValuePipe } from '../../pipes/currency-value.pipe';

@Component({
  selector: 'bk-input-calculator',
  templateUrl: './input-calculator.component.html',
  styleUrls: ['./input-calculator.component.css']
})
export class InputCalculatorComponent implements OnInit {
  @Input()
  public position: string;
  @Input()
  public placeholder: string;
  @Input()
  public value: number;
  @Input()
  public floatingPoint: number = 2;
  @Input()
  public skipDecimalZeros: boolean = true;
  @Input()
  public showConfirm: boolean = false;
  @Input()
  public confirmValue: boolean = false;

  @Output()
  public changeInputValue: EventEmitter<number> = new EventEmitter();
  @Output()
  public validInputValue: EventEmitter<boolean> = new EventEmitter();
  @Output()
  public changeConfirmValue: EventEmitter<boolean> = new EventEmitter();

  @ViewChild ('inputValue', {static : true})
  private _INPUT_ELEMENT_REF: ElementRef;
  private _INPUT: HTMLInputElement;
  private _focused: boolean = false;

  public constructor(private _currencyValuePipe: CurrencyValuePipe) {}

  public ngOnInit(): void {
    this._INPUT = this._INPUT_ELEMENT_REF.nativeElement;
    this._INPUT.value = this.value ? this._currencyValuePipe.transform(this.value, this.floatingPoint, this.skipDecimalZeros) : '';
  }

  public onConfirmValueChanged(value: MatSlideToggleChange): void {
    this.confirmValue = value.checked;
    this.changeConfirmValue.next(this.confirmValue);
  }

  public changeInput(): void {
    let inputValue: string = this._INPUT.value.replace(/\s/g, '');
    if (CurrencyUtils.ILLEGAL_CALCULATION_SYMBOLS_PATTERN.test(inputValue)) {
      this._INPUT.value = inputValue = inputValue.slice(0, -1);
    }

    if (inputValue) {
      this.validInputValue.next(!CurrencyUtils.LAST_SYMBOL_PATTERN.test(inputValue));
    }
  }

  public blurOrEnterClick(): void {
    this._focused = false;
    this.value = this.calculateValue();
    if (this.value === 0) {
      this.value = null;
    }

    this._INPUT.value = this._currencyValuePipe.transform(this.value, this.floatingPoint, this.skipDecimalZeros);
    this.changeInputValue.next(this.value);
    this.validInputValue.next(!CurrencyUtils.LAST_SYMBOL_PATTERN.test(this._INPUT.value));
  }

  public showCalculation(): boolean {
    return this._focused === true && CurrencyUtils.CALCULATION_PATTERN.test(this._INPUT.value);
  }

  public focusInput(): void {
    this._focused = true;
  }

  public calculateValue(): number {
    return CurrencyUtils.convertValue(this._INPUT.value.replace(/\s/g, ''));
  }

  public getInputClass(showCalculator: boolean): string {
    return `form-control 
            ${showCalculator && (this.position === 'right' || this.position === 'middle') ? 'no-left-radius' : ''} 
            ${!showCalculator && (this.position === 'left' || this.position === 'middle') ? 'no-right-radius' : ''} 
            ${InputCalculatorComponent.checkValueValidationClass(this._INPUT.value)}`;
  }

  private static checkValueValidationClass(value: string): string {
    if (value === '' || CurrencyUtils.ILLEGAL_CALCULATION_SYMBOLS_PATTERN.test(value)) {
      return '';
    }

    // tslint:disable-next-line:no-eval
    return CurrencyUtils.LAST_SYMBOL_PATTERN.test(value) || CurrencyUtils.convertValue(value) < 0.01 ? 'validation-fail' : 'validation-success';
  }
}
