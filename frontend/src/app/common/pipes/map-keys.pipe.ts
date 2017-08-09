import { Pipe, PipeTransform } from '@angular/core';

@Pipe({
  name: 'mapKeys'
})
export class MapKeysPipe implements PipeTransform {
  public transform(value: {[key: string]: any}): any {
    const keys: string[] = [];
    for (const key in value) {
      keys.push(key);
    }

    return keys;
  }
}
