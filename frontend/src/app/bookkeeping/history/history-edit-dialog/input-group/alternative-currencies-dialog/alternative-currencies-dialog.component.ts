import { Component, Inject } from '@angular/core';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material';

import { isNumeric } from 'rxjs/util/isNumeric';

import { CurrencyValuePipe } from '../../../../../common/pipes/currency-value.pipe';

@Component({
  selector: 'bk-alternative-currencies-dialog',
  templateUrl: './alternative-currencies-dialog.component.html',
  styleUrls: ['./alternative-currencies-dialog.component.css']
})
export class AlternativeCurrenciesDialogComponent {
  private _originalAlternativeCurrencies: {[key: string]: number};

  public constructor(
    @Inject(MAT_DIALOG_DATA) public data: {balance: HistoryBalanceType},
    private _dialogRef: MatDialogRef<AlternativeCurrenciesDialogComponent>,
    private _currencyValuePipe: CurrencyValuePipe
  ) {
    this._originalAlternativeCurrencies = Object.assign({}, this.data.balance.alternativeCurrency);
  }

  public getAmountValue(currency: string): string {
    const amount: number = this.data.balance.alternativeCurrency[currency] * this.data.balance.value;
    return this._currencyValuePipe.transform(amount, 2, true);
  }

  public calculateCurrencyValue(currency: string, amountInput: HTMLInputElement): void {
    if (isNumeric(amountInput.value)) {
      this.data.balance.alternativeCurrency[currency] = Number((amountInput.value / this.data.balance.value).toFixed(4));
    } else {
      amountInput.value = this.getAmountValue(currency);
    }
  }

  public save(): void {
    this.data.balance.alternativeCurrency = Object.assign({}, this.data.balance.alternativeCurrency);
    this._dialogRef.close();
  }

  public close(): void {
    this.data.balance.alternativeCurrency = this._originalAlternativeCurrencies;
    this._dialogRef.close();
  }
}
