import { Component, Inject } from '@angular/core';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material';

import { isNumeric } from 'rxjs/internal/util/isNumeric';

import { CurrencyValuePipe } from '../../../../common/pipes/currency-value.pipe';
import { ProfileService } from '../../../../common/service/profile.service';

@Component({
  selector: 'bk-alternative-currencies-dialog',
  templateUrl: './alternative-currencies-dialog.component.html',
  styleUrls: ['./alternative-currencies-dialog.component.css']
})
export class AlternativeCurrenciesDialogComponent {
  private readonly _ORIGINAL_ALTERNATIVE_CURRENCIES: {[key: string]: number};

  public constructor(
    @Inject(MAT_DIALOG_DATA) public data: {balance: HistoryBalanceType},
    private _profileService: ProfileService,
    private _dialogRef: MatDialogRef<AlternativeCurrenciesDialogComponent>,
    private _currencyValuePipe: CurrencyValuePipe
  ) {
    this._ORIGINAL_ALTERNATIVE_CURRENCIES = Object.assign({}, this.data.balance.alternativeCurrency);
  }

  public getCurrencyConversion(currency: string): string {
    return this._currencyValuePipe.transform(this.data.balance.alternativeCurrency[currency] / this.data.balance.value, 4, true);
  }

  public blurValueInCurrency(currency: string, amountInput: HTMLInputElement): void {
    if (isNumeric(amountInput.value) && amountInput.value !== '0') {
      this.data.balance.alternativeCurrency[currency] = Number(this._currencyValuePipe.transform(amountInput.value, 2, true));
    } else {
      amountInput.value = this.data.balance.alternativeCurrency[currency].toString();
    }
  }

  public save(): void {
    this.data.balance.alternativeCurrency = Object.assign({}, this.data.balance.alternativeCurrency);
    this._dialogRef.close();
  }

  public close(): void {
    this.data.balance.alternativeCurrency = this._ORIGINAL_ALTERNATIVE_CURRENCIES;
    this._dialogRef.close();
  }
}
