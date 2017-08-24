import { Injectable } from '@angular/core';

@Injectable()
export class CurrencyUtils {
  public static CALCULATION_PATTERN: RegExp = new RegExp('[\\+\\-\\*\\/]');
  public static ILLEGAL_CALCULATION_SYMBOLS_PATTERN: RegExp = new RegExp('[^0-9\\+\\-\\*\\/\\.\\,\']');
  public static LAST_SYMBOL_PATTERN: RegExp = new RegExp('[\\+\\-\\*\\/\\.\\,\']$');

  public static convertValue(value: string): number {
    value = value.replace(/\'/g, '').replace(',', '.');
    if (value.length === 0 || CurrencyUtils.ILLEGAL_CALCULATION_SYMBOLS_PATTERN.test(value)) {
      return 0;
    }

    if (CurrencyUtils.CALCULATION_PATTERN.test(value)) {
      return !CurrencyUtils.LAST_SYMBOL_PATTERN.test(value) ? eval(value) : 0;
    } else {
      return Number(value);
    }
  }
}
