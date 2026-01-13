import { Pipe, PipeTransform } from '@angular/core';

import { CurrencyService } from '../../service/currency.service';
import { CurrencyDetail } from '../../model/currency-detail';

export interface ConvertedSummaryValue {
  currency: string;
  value: number;
}

@Pipe({
    name: 'summaryValuesConversion',
    standalone: false
})
export class SummaryValuesConversionPipe implements PipeTransform {
  public constructor(private _currencyService: CurrencyService) {}

  public transform(values: {[currency: string]: number}, conversionCurrency: CurrencyDetail, year: number, month: number): ConvertedSummaryValue[] {
    if (!conversionCurrency) {
      // No conversion - return all currencies as-is
      return Object.entries(values).map(([currency, value]) => ({
        currency: currency,
        value: value
      }));
    }

    // Convert all values to the target currency using monthly average rates
    let totalConverted = 0;
    Object.entries(values).forEach(([currency, value]) => {
      totalConverted += this._currencyService.convertToAverageCurrency(
        value, currency, conversionCurrency.name, year, month
      );
    });

    return [{
      currency: conversionCurrency.name,
      value: totalConverted
    }];
  }
}
