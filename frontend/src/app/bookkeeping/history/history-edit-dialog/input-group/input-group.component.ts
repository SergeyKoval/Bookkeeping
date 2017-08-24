import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { MdDialog } from '@angular/material';

import { AlternativeCurrenciesDialogComponent } from './alternative-currencies-dialog/alternative-currencies-dialog.component';
import { CurrencyValueDirective } from 'app/common/directives/currency-value.directive';
import { CurrencyUtils } from '../../../../common/utils/currency-utils';

@Component({
  selector: 'bk-input-group',
  templateUrl: './input-group.component.html',
  styleUrls: ['./input-group.component.css']
})
export class InputGroupComponent implements OnInit {


  @Input()
  public selectedCurrency: string;
  @Input()
  public currencies: Currency[];
  @Input()
  public historyItem: HistoryType;
  @Input()
  public inputValue: number;

  @Output()
  public chooseCurrency: EventEmitter<Currency> = new EventEmitter();
  @Output()
  public changeInputValue: EventEmitter<number> = new EventEmitter();


  public constructor(private _dialog: MdDialog) { }

  public ngOnInit(): void {
  }

  public blurInput(): void {
    if (this.inputValue && this.inputValue > 0.1) {}
    this.changeInputValue.next(this.inputValue);
  }

  public changeCurrency(currency: Currency): void {
    if (currency.name !== this.selectedCurrency) {
      if (this.historyItem.type === 'expense' || this.historyItem.type === 'income') {
        this.historyItem.balance.alternativeCurrency = currency.conversions;
      }
      this.chooseCurrency.next(currency);
    }
  }

  public disableAlternativeCurrencies(balanceValue: string): boolean {
    return balanceValue === '' || CurrencyUtils.ILLEGAL_CALCULATION_SYMBOLS_PATTERN.test(balanceValue) || CurrencyUtils.LAST_SYMBOL_PATTERN.test(balanceValue) ? true : null;
  }

  public checkValueValidation(balanceValue: string): string {
    if (balanceValue === '') {
      return '';
    }

    return CurrencyUtils.ILLEGAL_CALCULATION_SYMBOLS_PATTERN.test(balanceValue) || CurrencyUtils.LAST_SYMBOL_PATTERN.test(balanceValue) ? 'validation-fail' : 'validation-success';
  }

  public openCurrenciesPopup(balanceValue: string): void {
    if (CurrencyUtils.convertValue(balanceValue) > 0) {
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
