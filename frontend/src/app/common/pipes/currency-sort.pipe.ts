import { Pipe, PipeTransform } from '@angular/core';

@Pipe({
  name: 'currencySort'
})
export class CurrencySortPipe implements PipeTransform {
  public transform(currencies: CurrencyDetail[]): CurrencyDetail[] {
    currencies.sort((a: CurrencyDetail, b: CurrencyDetail) => {
      if (!a.order) {
        return 1;
      }
      if (!b.order) {
        return -1;
      }
      return a.order - b.order;
    });
    return currencies;
  }
}
