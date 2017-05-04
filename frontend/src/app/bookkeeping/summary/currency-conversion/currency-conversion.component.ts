import {Component, EventEmitter, OnInit, Output} from '@angular/core';

import {CurrencyService} from '../../../common/service/currency.service';

@Component({
  selector: 'bk-currency-conversion',
  templateUrl: './currency-conversion.component.html',
  styleUrls: ['./currency-conversion.component.css']
})
export class CurrencyConversionComponent implements OnInit {
  @Output()
  public currencyConversion: EventEmitter<Currency> = new EventEmitter();

  public currencies: Currency[];
  public selectedCurrency: Currency = null;

  public constructor(private _currencyService: CurrencyService) { }

  public ngOnInit(): void {
    this._currencyService.currencies$.subscribe((currencies: Currency[]) => {
      this.currencies = currencies;
    });
  }

  public chooseCurrency(currency: Currency): void {
    this.selectedCurrency = currency;
    this.currencyConversion.emit(currency);
  }
}
