import { Pipe, PipeTransform } from '@angular/core';

@Pipe({
  name: 'mapKeys'
})
export class MapKeysPipe implements PipeTransform {
  public transform(value: {[key: string]: any}): string[] {
    return Object.keys(value);
  }
}
