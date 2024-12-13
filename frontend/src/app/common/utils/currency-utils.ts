import { Injectable } from '@angular/core';

@Injectable()
export class CurrencyUtils {
  public static CALCULATION_PATTERN: RegExp = new RegExp('[\\+\\-\\*\\/]');
  public static ILLEGAL_CALCULATION_SYMBOLS_PATTERN: RegExp = new RegExp('[^0-9\\+\\-\\*\\/\\.\\,\']');
  public static LAST_SYMBOL_PATTERN: RegExp = new RegExp('[\\+\\-\\*\\/\\.\\,\']$');
  public static HTML_SYMBOL_NUMBER_PATTERN: RegExp = new RegExp('&#(\\d+);');

  public static convertValue(value: string): number {
    value = value.replace(/\'/g, '').replace(/\,/g, '.');
    if (value.length === 0 || CurrencyUtils.ILLEGAL_CALCULATION_SYMBOLS_PATTERN.test(value)) {
      return 0;
    }

    if (CurrencyUtils.CALCULATION_PATTERN.test(value)) {
      const calculatedValue: number = this.safeEvaluateValue(value);
      return isFinite(calculatedValue) ? Number(calculatedValue.toFixed(2)) : 0;
    } else {
      return Number(value);
    }
  }

  public static convertValueToCurrency(value: number, valueCurrency: string, resultCurrency: string, alternativeCurrencies: {[key: string]: number}): number {
    if (valueCurrency === resultCurrency || value === 0) {
      return value || 0;
    }

    return Math.round(((value || 0) * alternativeCurrencies[resultCurrency]) * 100) / 100 ;
  }

  public static convertCodeToSymbol(htmlCode: string): string {
    if (!CurrencyUtils.HTML_SYMBOL_NUMBER_PATTERN.test(htmlCode)) {
      return htmlCode;
    }

    const htmlCodeNumber: number = Number(CurrencyUtils.HTML_SYMBOL_NUMBER_PATTERN.exec(htmlCode)[1]);
    return String.fromCharCode(htmlCodeNumber);
  }

  private static safeEvaluateValue(expression: string): number {
    if (CurrencyUtils.LAST_SYMBOL_PATTERN.test(expression)) {
      return 0;
    }

    return new Function(`return ${expression}`)();
  }
}
