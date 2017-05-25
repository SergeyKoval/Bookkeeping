import { Pipe, PipeTransform } from '@angular/core';

@Pipe({
  name: 'currencyValue'
})
export class CurrencyValuePipe implements PipeTransform {

  public transform(value: number, fixedSize: boolean): string {
    const stringValue: string = fixedSize ? value.toFixed(2) : value.toString();
    return stringValue.replace(/\B(?=(\d{3})+(?!\d))/g, '\'');
  }
}
