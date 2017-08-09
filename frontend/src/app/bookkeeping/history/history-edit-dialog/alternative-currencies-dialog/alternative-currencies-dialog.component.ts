import {Component, Inject} from '@angular/core';
import {MD_DIALOG_DATA, MdDialogRef} from '@angular/material';

import {CurrencyValuePipe} from '../../../../common/pipes/currency-value.pipe';
import {isNumeric} from 'rxjs/util/isNumeric';


@Component({
  selector: 'bk-alternative-currencies-dialog',
  templateUrl: './alternative-currencies-dialog.component.html',
  styleUrls: ['./alternative-currencies-dialog.component.css']
})
export class AlternativeCurrenciesDialogComponent {
  private _originalAlternativeCurrencies: {[key: string]: number};

  public constructor(
    @Inject(MD_DIALOG_DATA) public data: {balance: HistoryBalanceType},
    private _dialogRef: MdDialogRef<AlternativeCurrenciesDialogComponent>,
    private _currencyValuePipe: CurrencyValuePipe
  ) {
    this._originalAlternativeCurrencies = Object.assign({}, this.data.balance.alternativeCurrency);
  }

  public getAmountValue(currency: string): number {
    const amount: number = this.data.balance.alternativeCurrency[currency] * this.data.balance.value;
    return Number(this._currencyValuePipe.transform(amount, true));
  }

  public calculateCurrencyValue(currency: string, amountInput: HTMLInputElement): void {
    if (isNumeric(amountInput.value)) {
      this.data.balance.alternativeCurrency[currency] = Number((amountInput.value / this.data.balance.value).toFixed(4));
    } else {
      amountInput.value = this.getAmountValue(currency).toString();
    }
  }

  public save(): void {
    this._dialogRef.close();
  }

  public close(): void {
    this.data.balance.alternativeCurrency = this._originalAlternativeCurrencies;
    this._dialogRef.close();
  }
}
