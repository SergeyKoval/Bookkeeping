import { Component, Input } from '@angular/core';

import { ProfileService } from '../../service/profile.service';

@Component({
  selector: 'bk-currency-symbol',
  template: `<span [innerHTML]="currencyCode"></span>`
})
export class CurrencySymbolComponent {
  @Input()
  public set currencyName(currencyName: string) {
    this.currencyCode = this._authenticationService.getCurrencyDetails(currencyName).symbol;
  }

  public currencyCode: string;

  public constructor(private _authenticationService: ProfileService) {}
}
