import { Component, Inject, OnInit } from '@angular/core';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material';

import { ProfileService } from '../../../common/service/profile.service';
import { CurrencyUtils } from '../../../common/utils/currency-utils';

@Component({
  selector: 'bk-balance-dialog',
  templateUrl: './balance-dialog.component.html',
  styleUrls: ['./balance-dialog.component.css']
})
export class BalanceDialogComponent implements OnInit {
  public currencies: CurrencyDetail[];
  public loading: boolean = true;
  public errors: string;

  public constructor(
    @Inject(MAT_DIALOG_DATA) public data: {subAccountBalance: {[currency: string]: number}},
    private _dialogRef: MatDialogRef<BalanceDialogComponent>,
    private _profileService: ProfileService
  ) {}

  public ngOnInit(): void {
    this.currencies = this._profileService.authenticatedProfile.currencies;
  }

  public close(): void {
    this._dialogRef.close(null);
  }

  public save(): void {
    const filterResult: [string, number][] = Object.entries(this.data.subAccountBalance).filter(([currency, value]) => value < 0 || value >= 10000000);
    if (filterResult.length > 0) {
      this.errors = `Недопустимое значение для ${CurrencyUtils.convertCodeToSymbol(this._profileService.getCurrencyDetails(filterResult[0][0]).symbol)}`;
    } else {
      this._dialogRef.close(this.data.subAccountBalance);
    }
  }

  public getCurrencyValue(currency: CurrencyDetail): number {
    return this.data.subAccountBalance[currency.name] || 0;
  }

  public setCurrencyValue(currencyDetails: CurrencyDetail, currencyValue: number): void {
    this.data.subAccountBalance[currencyDetails.name] = Number(currencyValue);
  }
}
