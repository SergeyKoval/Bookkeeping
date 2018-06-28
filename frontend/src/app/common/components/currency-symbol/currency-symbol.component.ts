import { Component, Input } from '@angular/core';

import { ProfileService } from '../../service/profile.service';

@Component({
  selector: 'bk-currency-symbol',
  template: `<span [innerHTML]="currencyCode"></span>`
})
export class CurrencySymbolComponent {
  @Input()
  public set currencyName(currencyName: string) {
    const currencyDetails = this._authenticationService.getCurrencyDetails(currencyName);
    this.currencyCode = currencyDetails ? currencyDetails.symbol : currencyName;
  }

  public currencyCode: string;

  public constructor(private _authenticationService: ProfileService) {}
}
