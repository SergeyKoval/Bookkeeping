import { Pipe, PipeTransform } from '@angular/core';

@Pipe({
  name: 'currencyValue'
})
export class CurrencyValuePipe implements PipeTransform {
  public transform(value: number, fixedSize?: number, skipDecimalZeros?: boolean): string {
    if (!value && value !== 0) {
      return null;
    }

    let stringValue: string = fixedSize || fixedSize === 0 ? Number(value).toFixed(fixedSize) : value.toString();
    if (skipDecimalZeros && stringValue.includes('.')) {
      while (stringValue.endsWith('0')) {
        stringValue = stringValue.slice(0, -1);
      }
      if (stringValue.endsWith('.')) {
        stringValue = stringValue.slice(0, -1);
      }
    }
    return stringValue.replace(stringValue.includes('.') ? /\B(?=(\d{3})+(?!\d).)/g : /\B(?=(\d{3})+(?!\d)$)/g, '\'');
  }
}
