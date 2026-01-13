import { Pipe, PipeTransform } from '@angular/core';

import { CurrencyDetail } from '../../model/currency-detail';
import { HistoryBalanceType } from '../../model/history/history-balance-type';

export interface ConvertedHistoryValue {
  value: number;
  currency: string;
}

@Pipe({
    name: 'historyValueConversion',
    standalone: false
})
export class HistoryValueConversionPipe implements PipeTransform {

  public transform(balance: HistoryBalanceType, conversionCurrency: CurrencyDetail): ConvertedHistoryValue {
    if (!conversionCurrency) {
      return {
        value: balance.value || 0,
        currency: balance.currency
      };
    }

    // If the target currency is the same as the transaction currency, return original value
    if (conversionCurrency.name === balance.currency) {
      return {
        value: balance.value || 0,
        currency: balance.currency
      };
    }

    // Look up the converted value from alternativeCurrency
    const alternativeValue = balance.alternativeCurrency?.[conversionCurrency.name];
    if (alternativeValue !== undefined) {
      return {
        value: alternativeValue,
        currency: conversionCurrency.name
      };
    }

    // Fallback to original value if no conversion available
    return {
      value: balance.value || 0,
      currency: balance.currency
    };
  }
}
