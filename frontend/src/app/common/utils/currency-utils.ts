import { Injectable } from '@angular/core';

@Injectable()
export class CurrencyUtils {
  public static CALCULATION_PATTERN: RegExp = new RegExp('[\\+\\-\\*\\/]');
  public static ILLEGAL_CALCULATION_SYMBOLS_PATTERN: RegExp = new RegExp('[^0-9\\+\\-\\*\\/\\.\\,\']');
  public static LAST_SYMBOL_PATTERN: RegExp = new RegExp('[\\+\\-\\*\\/\\.\\,\']$');

  public static convertValue(value: string): number {
    value = value.replace(/\'/g, '').replace(/\,/g, '.');
    if (value.length === 0 || CurrencyUtils.ILLEGAL_CALCULATION_SYMBOLS_PATTERN.test(value)) {
      return 0;
    }

    if (CurrencyUtils.CALCULATION_PATTERN.test(value)) {
      const calculatedValue: number = !CurrencyUtils.LAST_SYMBOL_PATTERN.test(value) ? eval(value) : 0;  // tslint:disable-line:no-eval
      return isFinite(calculatedValue) ? Number(calculatedValue.toFixed(2)) : 0;
    } else {
      return Number(value);
    }
  }

  public static convertValueToCurrency(value: number, valueCurrency: string, resultCurrency: string, alternativeCurrencies: {[key: string]: number}): number {
    if (valueCurrency === resultCurrency) {
      return value || 0;
    }

    return Math.round(((value || 0) * alternativeCurrencies[resultCurrency]) * 100) / 100 ;
  }
}
