import { Component, Input } from '@angular/core';

import { ProfileService } from '../../service/profile.service';
import { CurrencyDetail } from '../../model/currency-detail';

@Component({
  selector: 'bk-currency-symbol',
  template: `<span [innerHTML]="currencyCode"></span>`
})
export class CurrencySymbolComponent {
  @Input()
  public set currencyName(currencyName: string) {
    const currencyDetails: CurrencyDetail = this._authenticationService.getCurrencyDetails(currencyName);
    this.currencyCode = currencyDetails ? currencyDetails.symbol : currencyName;
  }

  public currencyCode: string;

  public constructor(private _authenticationService: ProfileService) {}
}
