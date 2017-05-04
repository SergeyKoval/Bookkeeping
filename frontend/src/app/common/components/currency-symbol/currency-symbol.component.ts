import {Component, Input} from '@angular/core';

import {CurrencyService} from '../../service/currency.service';

@Component({
  selector: 'bk-currency-symbol',
  template: `<span [innerHTML]="currencyCode"></span>`
})
export class CurrencySymbolComponent {
  @Input()
  public set currencyName(currencyName: string) {
    this.currencyCode = this._currencyService.getCurrencySymbol(currencyName);
  }

  public currencyCode: string;


  public constructor(private _currencyService: CurrencyService) {}
}
