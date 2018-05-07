import { Component, EventEmitter, Input, Output } from '@angular/core';
import { MatDialog } from '@angular/material';

import { AlternativeCurrenciesDialogComponent } from './alternative-currencies-dialog/alternative-currencies-dialog.component';
import { CurrencyUtils } from '../../../common/utils/currency-utils';
import { CurrencyService } from '../../../common/service/currency.service';

@Component({
  selector: 'bk-input-group',
  templateUrl: './input-group.component.html',
  styleUrls: ['./input-group.component.css']
})
export class InputGroupComponent {
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

  @Output()
  public chooseCurrency: EventEmitter<CurrencyDetail> = new EventEmitter();
  @Output()
  public changeInputValue: EventEmitter<number> = new EventEmitter();

  public constructor(
    private _dialog: MatDialog,
    private _currencyService: CurrencyService
  ) { }

  public blurInput(): void {
    if (this.inputValue && this.inputValue > 0.1) {}
    this.changeInputValue.next(this.inputValue);
  }

  public changeCurrency(currency: CurrencyDetail): void {
    if (currency.name !== this.selectedCurrency) {
      this.chooseCurrency.next(currency);
    }
  }

  public showAlternativeCurrenciesButton(): boolean {
    return this.historyItem.type === 'expense' || this.historyItem.type === 'income';
  }

  public disableAlternativeCurrencies(balanceValue: string): boolean {
    return this.alternativeCurrencyLoading || balanceValue === '' || CurrencyUtils.ILLEGAL_CALCULATION_SYMBOLS_PATTERN.test(balanceValue)
      || CurrencyUtils.LAST_SYMBOL_PATTERN.test(balanceValue) ? true : null;
  }

  public checkValueValidation(balanceValue: string): string {
    if (balanceValue === '') {
      return '';
    }

    return CurrencyUtils.ILLEGAL_CALCULATION_SYMBOLS_PATTERN.test(balanceValue) || CurrencyUtils.LAST_SYMBOL_PATTERN.test(balanceValue) ? 'validation-fail' : 'validation-success';
  }

  public openCurrenciesPopup(balanceValue: string): void {
    if (!this.alternativeCurrencyLoading && CurrencyUtils.convertValue(balanceValue) > 0) {
      this._dialog.open(AlternativeCurrenciesDialogComponent, {
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

  public getValuePlaceholder(): string {
    if (this.historyItem.type === 'exchange') {
      return 'Было';
    }

    return 'Сумма';
  }
}
