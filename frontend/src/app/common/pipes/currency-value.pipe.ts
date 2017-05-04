import { Pipe, PipeTransform } from '@angular/core';

@Pipe({
  name: 'currencyValue'
})
export class CurrencyValuePipe implements PipeTransform {

  public transform(value: number): string {
    return value.toFixed(2).replace(/\B(?=(\d{3})+(?!\d))/g, '\'');
  }
}
