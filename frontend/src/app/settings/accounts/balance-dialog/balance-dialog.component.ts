import { Component, Inject, OnInit } from '@angular/core';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material';

import { CurrencyService } from '../../../common/service/currency.service';
import { ProfileService } from '../../../common/service/profile.service';

@Component({
  selector: 'bk-balance-dialog',
  templateUrl: './balance-dialog.component.html',
  styleUrls: ['./balance-dialog.component.css']
})
export class BalanceDialogComponent implements OnInit {
  public currencies: CurrencyDetail[];
  public loading: boolean = true;

  public constructor(
    @Inject(MAT_DIALOG_DATA) public data: {subAccountBalance: BalanceItem[]},
    private _dialogRef: MatDialogRef<BalanceDialogComponent>,
    private _currencyService: CurrencyService,
    private _profileService: ProfileService
  ) {}

  public ngOnInit(): void {
    this.currencies = this._profileService.authenticatedProfile.currencies;
  }

  public close(): void {
    this._dialogRef.close(null);
  }

  public save(): void {
    this._dialogRef.close(this.data.subAccountBalance);
  }

  public getCurrencyValue(currency: CurrencyDetail): number {
    const result: BalanceItem = this.data.subAccountBalance.filter((balanceItem: BalanceItem) => balanceItem.currency === currency.name)[0];
    return result ? result.value : 0;
  }

  public setCurrencyValue(currencyDetails: CurrencyDetail, currencyValue: number): void {
    const valueNumber: number = Number(currencyValue);
    let result: BalanceItem = this.data.subAccountBalance.filter((balanceItem: BalanceItem) => balanceItem.currency === currencyDetails.name)[0];

    if (!result) {
      result = {currency: currencyDetails.name, value: valueNumber};
      this.data.subAccountBalance.push(result);
      return;
    }

    result.value = valueNumber;
  }
}
