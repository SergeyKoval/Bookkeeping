import { Pipe, PipeTransform } from '@angular/core';
import { ProfileService } from '../service/profile.service';
import { CurrencyDetail } from '../model/currency-detail';

@Pipe({
    name: 'currencySort',
    standalone: false
})
export class CurrencySortPipe implements PipeTransform {

  public constructor(private _profileService: ProfileService) {}

  public transform(currencies: string[]): string[] {
    currencies.sort((a: string, b: string) => {
      const aDetails: CurrencyDetail = this._profileService.getCurrencyDetails(a);
      if (!aDetails || !aDetails.order) {
        return 1;
      }

      const bDetails: CurrencyDetail = this._profileService.getCurrencyDetails(b);
      if (!bDetails || !bDetails.order) {
        return -1;
      }
      return aDetails.order - bDetails.order;
    });
    return currencies;
  }
}
