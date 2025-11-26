import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';

import { AlternativeCurrenciesDialogComponent } from './alternative-currencies-dialog/alternative-currencies-dialog.component';
import { CurrencyUtils } from '../../../common/utils/currency-utils';
import { CurrencyDetail } from '../../../common/model/currency-detail';
import { HistoryType } from '../../../common/model/history/history-type';

@Component({
    selector: 'bk-input-group',
    templateUrl: './input-group.component.html',
    styleUrls: ['./input-group.component.css'],
    standalone: false
})
export class InputGroupComponent implements OnInit {
  @Input()
  public selectedCurrency: string;
  @Input()
  public currencies: CurrencyDetail[];
  @Input()
  public historyItem: HistoryType;
  @Input()
  public inputValue: number;
  @Input()
  public alternativeCurrencyLoading: boolean;
  @Input()
  public placeholder: string;

  @Output()
  public chooseCurrency: EventEmitter<CurrencyDetail> = new EventEmitter();
  @Output()
  public changeInputValue: EventEmitter<number> = new EventEmitter();

  public enableCurrencies: boolean;

  public constructor(private _dialog: MatDialog) {}

  public ngOnInit(): void {
    this.enableCurrencies = this.inputValue && this.inputValue > 0;
  }

  public changeValue(value: number): void {
    this.changeInputValue.next(value);
  }

  public changeValueValidation(validation: boolean): void {
    this.enableCurrencies = validation;
  }

  public changeCurrency(currency: CurrencyDetail): void {
    if (currency.name !== this.selectedCurrency) {
      this.chooseCurrency.next(currency);
    }
  }

  public showAlternativeCurrenciesButton(): boolean {
    return this.historyItem.type === 'expense' || this.historyItem.type === 'income';
  }

  public disableAlternativeCurrencies(): boolean {
    return this.alternativeCurrencyLoading || !this.enableCurrencies ? true : null;
  }

  public checkValueValidation(balanceValue: string): string {
    if (balanceValue === '') {
      return '';
    }

    return CurrencyUtils.ILLEGAL_CALCULATION_SYMBOLS_PATTERN.test(balanceValue) || CurrencyUtils.LAST_SYMBOL_PATTERN.test(balanceValue) ? 'validation-fail' : 'validation-success';
  }

  public openCurrenciesPopup(): void {
    if (!this.alternativeCurrencyLoading && this.enableCurrencies && this.inputValue > 0) {
      this._dialog.open(AlternativeCurrenciesDialogComponent, {
        id: 'alternative-currencies-dialog',
        disableClose: true,
        width: '470px',
        data: {
          balance: this.historyItem.balance
        }
      });
    }
  }

  public showCalculation(balanceValue: string): boolean {
    return CurrencyUtils.CALCULATION_PATTERN.test(balanceValue) &&
      !CurrencyUtils.ILLEGAL_CALCULATION_SYMBOLS_PATTERN.test(balanceValue) &&
      !CurrencyUtils.LAST_SYMBOL_PATTERN.test(balanceValue);
  }

  public calculateValue(balanceValue: string): number {
    return CurrencyUtils.convertValue(balanceValue);
  }
}
