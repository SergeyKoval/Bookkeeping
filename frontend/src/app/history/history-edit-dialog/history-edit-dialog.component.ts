import { Component, ElementRef, Inject, OnInit, ViewChild } from '@angular/core';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material';

import { Observable } from 'rxjs/index';
import { filter, tap } from 'rxjs/internal/operators';
import { IMyDate, IMyDateModel, IMyDpOptions } from 'mydatepicker';

import { HistoryService } from '../../common/service/history.service';
import { CurrencyService } from '../../common/service/currency.service';
import { ProfileService } from '../../common/service/profile.service';
import { LoadingDialogComponent } from '../../common/components/loading-dialog/loading-dialog.component';
import { LoadingService } from '../../common/service/loading.service';
import { AlertService } from '../../common/service/alert.service';

@Component({
  selector: 'bk-history-edit-dialog',
  templateUrl: './history-edit-dialog.component.html',
  styleUrls: ['./history-edit-dialog.component.css']
})
export class HistoryEditDialogComponent implements OnInit {
  public datePickerOptions: IMyDpOptions = {dateFormat: 'dd.mm.yyyy', inline: true, selectorWidth: '210px', selectorHeight: '210px'};

  public errors: string;
  public historyItem: HistoryType;
  public currencies: CurrencyDetail[];
  public selectedDate: IMyDate;
  public accounts: SelectItem[];
  public categories: SelectItem[];
  public selectedAccount: SelectItem[];
  public selectedToAccount: SelectItem[];
  public selectedCategory: SelectItem[];
  public alternativeCurrencyLoading: boolean;

  @ViewChild('title')
  private _titleElement: ElementRef;
  private _allCategories: Category[];
  private _goalStatusChange: boolean = false;

  public constructor(
    public dialogRef: MatDialogRef<HistoryEditDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: {historyItem: HistoryType, editMode: boolean},
    private _dialogRef: MatDialogRef<HistoryEditDialogComponent>,
    private _historyService: HistoryService,
    private _currencyService: CurrencyService,
    private _authenticationService: ProfileService,
    private _loadingService: LoadingService,
    private _alertService: AlertService
  ) {}

  public ngOnInit(): void {
    const today = new Date(Date.now());
    this.historyItem = this.data.historyItem || this.initNewHistoryItem('expense', today.getFullYear(), today.getMonth() + 1, today.getDate());
    this.selectedDate = {year: this.historyItem.year, month: this.historyItem.month, day: this.historyItem.day};

    this.alternativeCurrencyLoading = !this._currencyService.isCurrencyHistoryLoaded(this.historyItem.balance.currency, this.selectedDate);
    if (this.alternativeCurrencyLoading) {
      this.loadAlternativeCurrencies(this.selectedDate);
    }

    const profile: Profile = this._authenticationService.authenticatedProfile;
    this.currencies = profile.currencies;
    this._allCategories = profile.categories;
    this.categories = this._authenticationService.transformCategories(this._allCategories, this.historyItem.type);
    this._authenticationService.accounts$.subscribe((accounts: FinAccount[]) => this.accounts = this._authenticationService.transformAccounts(accounts));

    if (this.data.editMode) {
      this.selectedAccount = ProfileService.chooseSelectedItem(this.accounts, this.historyItem.balance.account, this.historyItem.balance.subAccount);
      this.selectedCategory = ProfileService.chooseSelectedItem(this.categories, this.historyItem.category, this.historyItem.subCategory);
      if (this.historyItem.type === 'transfer') {
        this.selectedToAccount = ProfileService.chooseSelectedItem(this.accounts, this.historyItem.balance.accountTo, this.historyItem.balance.subAccountTo);
      }
    }
  }

  public onDateChanged(event: IMyDateModel): void {
    this._titleElement.nativeElement.click();
    const date: IMyDate = event.date;
    if (date.day !== this.selectedDate.day || date.month !== this.selectedDate.month || date.year !== this.selectedDate.year) {
      if ((this.selectedDate.month !== date.month || this.selectedDate.year !== date.year)
        && !this._currencyService.isCurrencyHistoryLoaded(this.historyItem.balance.currency, date)) {

        this.alternativeCurrencyLoading = true;
        this.historyItem.balance.alternativeCurrency = {};
        this.loadAlternativeCurrencies(date);
      } else {
        this.historyItem.balance.alternativeCurrency = this._currencyService.getCurrencyHistoryConversions(this.historyItem.balance.currency, date);
      }
      this.selectedDate = date;
      this.historyItem.year = date.year;
      this.historyItem.month = date.month;
      this.historyItem.day = date.day;
    }
  }

  public changeCurrency(currency: CurrencyDetail): void {
    this.historyItem.balance.currency = currency.name;
    if (this.historyItem.type === 'expense' || this.historyItem.type === 'income') {
      if (!this._currencyService.isCurrencyHistoryLoaded(this.historyItem.balance.currency, this.selectedDate)) {
        this.alternativeCurrencyLoading = true;
        this.historyItem.balance.alternativeCurrency = {};
        this.loadAlternativeCurrencies(this.selectedDate);
      } else {
        this.historyItem.balance.alternativeCurrency = this._currencyService.getCurrencyHistoryConversions(this.historyItem.balance.currency, this.selectedDate);
      }
    }
  }

  public onChangeSelectedType(type: string): void {
    if (!this.isTypeSelected(type) && !this.data.editMode) {
      if (this.selectedCategory) {
        this.selectedCategory.length = 0;
      }
      if (this.selectedToAccount) {
        this.selectedToAccount.length = 0;
      }

      this.historyItem = this.initNewHistoryItemFromExisting(type, this.historyItem);
      this.categories = this._authenticationService.transformCategories(this._allCategories, this.historyItem.type);
    }
  }

  public isTypeSelected(type: string): boolean {
    return this.historyItem.type === type;
  }

  public save(): void {
    let validationResult: boolean;
    switch (this.historyItem.type) {
      case 'expense':
      case 'income':
        this.historyItem.balance.accountTo = null;
        this.historyItem.balance.subAccountTo = null;
        this.historyItem.balance.newCurrency = null;
        this.historyItem.balance.newValue = null;
        validationResult = this.validateExpenseOrIncome();
        break;
      case 'transfer':
        this.historyItem.balance.alternativeCurrency = null;
        this.historyItem.balance.newCurrency = null;
        this.historyItem.balance.newValue = null;
        this.historyItem.category = null;
        this.historyItem.subCategory = null;
        validationResult = this.validateTransfer();
        break;
      case 'exchange':
        this.historyItem.balance.alternativeCurrency = null;
        this.historyItem.category = null;
        this.historyItem.subCategory = null;
        validationResult = this.validateExchange();
        break;
    }

    if (validationResult) {
      const mdDialogRef: MatDialogRef<LoadingDialogComponent> = this._loadingService.openLoadingDialog('Сохранение...');
      this.processSaveResult(mdDialogRef, this.data.editMode
        ? this._historyService.editHistoryItem(this.historyItem)
        : this._historyService.addHistoryItem(this.historyItem));
    }
  }

  public changeNewCurrency(currency: CurrencyDetail): void {
    this.historyItem.balance.newCurrency = currency.name;
  }

  public close(refreshHistoryItems: boolean): void {
    this._dialogRef.close(refreshHistoryItems);
  }

  private initNewHistoryItem(historyType: string, year: number, month: number, day: number, balanceValue?: number, balanceCurrency?: string, balanceNewCurrency?: string,
                             balanceAlternativeCurrency?: {[key: string]: number}, balanceAccount?: string, balanceSubAccount?: string, historyDescription?: string): HistoryType {

    const currencyName: string = balanceCurrency || this._authenticationService.defaultCurrency.name;
    const result: HistoryType = {
      type: historyType,
      year: year,
      month: month,
      day: day,
      description: historyDescription,
      balance: {
        value: balanceValue,
        account: balanceAccount,
        subAccount: balanceSubAccount,
        currency: currencyName,
        alternativeCurrency: balanceAlternativeCurrency || this._currencyService.getCurrencyHistoryConversions(currencyName, this.selectedDate)
      }
    };

    if (historyType === 'exchange') {
      result.balance.newCurrency = balanceNewCurrency || this._authenticationService.defaultCurrency.name;
    }

    return result;
  }

  private validateExpenseOrIncome(): boolean {
    const balance: HistoryBalanceType = this.historyItem.balance;
    if (!this.commonValidation(balance)) {
      return false;
    }

    if (!this.selectedCategory || this.selectedCategory.length < 2) {
      this.errors = 'Категория не выбрана';
      return false;
    }
    this.historyItem.category = this.selectedCategory[0].title;
    this.historyItem.subCategory = this.selectedCategory[1].title;

    return true;
  }

  private commonValidation(balance: HistoryBalanceType): boolean {
    if (!balance.value || balance.value < 0.01) {
      this.errors = 'Сумма указана неверно';
      return false;
    }

    if (!this.selectedAccount || this.selectedAccount.length < 2) {
      this.errors = 'Счет не выбран';
      return false;
    }
    balance.account = this.selectedAccount[0].title;
    balance.subAccount = this.selectedAccount[1].title;

    return true;
  }

  private loadAlternativeCurrencies(date: IMyDate): void {
    this._currencyService.loadCurrenciesForMonth({month: date.month, year: date.year, currencies: this._authenticationService.getProfileCurrencies()})
      .subscribe(() => {
        this.historyItem.balance.alternativeCurrency = this._currencyService.getCurrencyHistoryConversions(this.historyItem.balance.currency, date);
        this.alternativeCurrencyLoading = false;
      });
  }

  private initNewHistoryItemFromExisting(historyType: string, originalItem: HistoryType): HistoryType {
    const balance: HistoryBalanceType = originalItem.balance;
    return this.initNewHistoryItem(historyType, originalItem.year, originalItem.month, originalItem.day,
      balance.value, balance.currency, balance.newCurrency, balance.alternativeCurrency, balance.account, balance.subAccount, originalItem.description);
  }

  private validateTransfer(): boolean {
    const balance: HistoryBalanceType = this.historyItem.balance;
    if (!this.commonValidation(balance)) {
      return false;
    }

    if (!this.selectedToAccount || this.selectedToAccount.length < 2) {
      this.errors = 'Счет не выбран';
      return false;
    }
    const accountTo: string = this.selectedToAccount[0].title;
    const subAccountTo: string = this.selectedToAccount[1].title;
    if (balance.account === accountTo && balance.subAccount === subAccountTo) {
      this.selectedToAccount.length = 0;
      this.errors = 'Неверный счет получателя';
      return false;
    }
    balance.accountTo = accountTo;
    balance.subAccountTo = subAccountTo;

    return true;
  }

  private validateExchange(): boolean {
    const balance: HistoryBalanceType = this.historyItem.balance;
    if (!this.commonValidation(balance)) {
      return false;
    }

    if (!balance.newValue || balance.newValue < 0.01) {
      this.errors = 'Сумма указана неверно';
      return false;
    }

    if (balance.currency === balance.newCurrency) {
      this.errors = 'Валюты не могут совпадать';
      return false;
    }

    return true;
  }

  private processSaveResult(mdDialogRef: MatDialogRef<LoadingDialogComponent>, result: Observable<SimpleResponse>): void {
    result.pipe(
      tap(simpleResponse => {
        if (simpleResponse.status === 'FAIL') {
          this.errors = 'Ошибка при сохранении';
          mdDialogRef.close();
        }
      }),
      filter(simpleResponse => simpleResponse.status === 'SUCCESS'),
    ).subscribe((response: SimpleResponse) => {
      this._authenticationService.quiteReloadAccounts();
      mdDialogRef.close();
      this.close(true);
    });
  }

  // public showGoalContainer(): boolean {
  //   return (this.isTypeSelected('expense') || this.isTypeSelected('income')) && this.selectedCategory && this.selectedCategory.length === 2;
  // }

  // public onSelectedGoalStatusChange(status: boolean): void {
  //   this._goalStatusChange = status;
  // }
}
