import { Pipe, PipeTransform } from '@angular/core';

@Pipe({
  name: 'assetImage'
})
export class AssetImagePipe implements PipeTransform {

  public transform(imagePath: string, folders: string): string {
    return `assets/image/${folders}/${imagePath}`;
  }
}
