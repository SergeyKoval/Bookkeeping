import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';

import { select, Store } from '@ngrx/store';
import { Observable } from 'rxjs';

import { AlertType } from '../../model/alert/AlertType';
import * as fromUser from '../../redux/reducers/user';
import { UserActions } from '../../redux/actions';
import { Currency } from '../../redux/reducers/user/currency.reducer';
import { fromCurrencies } from '../../redux/reducers';

@Component({
  selector: 'bk-currency-conversion',
  templateUrl: './currency-conversion.component.html',
  styleUrls: ['./currency-conversion.component.css']
})
export class CurrencyConversionComponent implements OnInit {
  @Input()
  public label: string = 'к валюте:';
  @Input()
  public selectedCurrency: Currency = null;
  @Input()
  public denyNoChoice: boolean = false;
  @Output()
  public currencyConversion: EventEmitter<Currency> = new EventEmitter();

  public currencies$: Observable<Array<Currency>>;

  public constructor(
    private _userStore: Store<fromUser.State>,
    private _currenciesStore: Store<fromCurrencies.CurrencyHistoryState>
  ) {}

  public ngOnInit(): void {
    this.currencies$ = this._userStore.pipe(select(fromUser.selectCurrencies));
  }

  public chooseCurrency(currency: Currency): void {
    if (currency === null) {
      this.useCurrency(currency);
      return;
    }

    this._currenciesStore.select(fromCurrencies.isCurrencyHistoryLoaded, { currency: currency.name }).subscribe(currencyHistoryLoaded => {
      if (currencyHistoryLoaded) {
        this.useCurrency(currency);
      } else {
        this._userStore.dispatch(UserActions.SHOW_ALERT({ alert: { type: AlertType.WARNING, message: 'Альтернативные валюты еще не загружены. Попробуйте немного позже.' } }));
      }
    });
  }

  private useCurrency(currency: Currency): void {
    this.selectedCurrency = currency;
    this.currencyConversion.emit(currency);
  }
}
