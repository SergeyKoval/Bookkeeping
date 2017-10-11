import { Component, ElementRef, Inject, OnInit, ViewChild } from '@angular/core';
import { Response } from '@angular/http';
import { MD_DIALOG_DATA, MdDialog, MdDialogRef } from '@angular/material';

import { IMyDate, IMyDateModel, IMyDpOptions } from 'mydatepicker';
import { HistoryService } from '../../../common/service/history.service';
import { CurrencyService } from '../../../common/service/currency.service';
import { SettingsService } from '../../../common/service/settings.service';
import { DateUtils } from '../../../common/utils/date-utils';
import { AuthenticationService } from '../../../common/service/authentication.service';
import { LoadingDialogComponent } from '../../../common/components/loading-dialog/loading-dialog.component';
import { LoadingService } from '../../../common/service/loading.service';
import { AlertService } from '../../../common/service/alert.service';
import { AlertType } from '../../../common/model/alert/AlertType';
import { Alert } from '../../../common/model/alert/Alert';

@Component({
  selector: 'bk-history-edit-dialog',
  templateUrl: './history-edit-dialog.component.html',
  styleUrls: ['./history-edit-dialog.component.css']
})
export class HistoryEditDialogComponent implements OnInit {
  public datePickerOptions: IMyDpOptions = {dateFormat: 'dd.mm.yyyy', inline: true};

  public errors: string;
  public historyItem: HistoryType;
  public currencies: Currency[];
  public selectedDate: IMyDate;
  public accounts: SelectItem[];
  public categories: SelectItem[];
  public selectedAccount: SelectItem[];
  public selectedToAccount: SelectItem[];
  public selectedCategory: SelectItem[];

  @ViewChild('title')
  private _titleElement: ElementRef;
  private _allCategories: Category[];
  private _goalStatusChange: boolean = false;

  public constructor(
    public dialogRef: MdDialogRef<HistoryEditDialogComponent>,
    @Inject(MD_DIALOG_DATA) public data: {historyItem: HistoryType, editMode: boolean},
    private _dialogRef: MdDialogRef<HistoryEditDialogComponent>,
    private _historyService: HistoryService,
    private _currencyService: CurrencyService,
    private _settingsService: SettingsService,
    private _authenticationService: AuthenticationService,
    private _loadingService: LoadingService,
    private _alertService: AlertService,
    private _dialog: MdDialog
  ) {}

  public ngOnInit(): void {
    this.historyItem = this.data.historyItem || this.initNewHistoryItem('expense', DateUtils.getUTCDate());
    this.selectedDate = DateUtils.getDateFromUTC(this.historyItem.date);

    this._currencyService.currencies$.subscribe((currencies: Currency[]) => this.currencies = currencies);
    this._settingsService.accounts$.subscribe((accounts: FinAccount[]) => this.accounts = this._settingsService.transformAccounts(accounts));
    this._settingsService.categories$.subscribe((categories: Category[]) => {
      this._allCategories = categories;
      this.categories = this._settingsService.transformCategories(categories, this.historyItem.type);
    });

    if (this.data.editMode) {
      this.selectedAccount = SettingsService.chooseSelectedItem(this.accounts, this.historyItem.balance.account, this.historyItem.balance.subAccount);
      this.selectedCategory = SettingsService.chooseSelectedItem(this.categories, this.historyItem.category, this.historyItem.subCategory);
    }
  }

  public onDateChanged(event: IMyDateModel): void {
    this._titleElement.nativeElement.click();
    this.selectedDate = event.date;
    this.historyItem.date = DateUtils.getUTCDateByDay(this.selectedDate);
  }

  public onChangeSelectedType(type: string): void {
    if (!this.isTypeSelected(type)) {
      if (this.selectedCategory) {
        this.selectedCategory.length = 0;
      }
      if (this.selectedToAccount) {
        this.selectedToAccount.length = 0;
      }

      this.historyItem = this.initNewHistoryItemFromExisting(type, this.historyItem);
      this.categories = this._settingsService.transformCategories(this._allCategories, this.historyItem.type);
    }
  }

  public changeCurrency(currency: Currency): void {
    this.historyItem.balance.currency = currency.name;
  }

  public changeNewCurrency(currency: Currency): void {
    this.historyItem.balance.newCurrency = currency.name;
  }

  public save(): void {
    let validationResult: boolean;
    switch (this.historyItem.type) {
      case 'expense':
      case 'income':
        validationResult = this.validateExpenseOrIncome();
        break;
      case 'transfer':
        validationResult = this.validateTransfer();
        break;
      case 'exchange':
        validationResult = this.validateExchange();
        break;
    }

    if (validationResult) {
      const mdDialogRef: MdDialogRef<LoadingDialogComponent> = this._loadingService.openLoadingDialog('Добавление...');
      this._historyService.addHistoryItem(this.historyItem).subscribe((response: Response) => {
        const alert: Alert = response.ok ? new Alert(AlertType.SUCCESS, 'Операция успешно добавлена') : new Alert(AlertType.WARNING, 'При добавлении возникла ошибка', null, 10);
        this._alertService.addAlertObject(alert);

        const ownerId: number = this._authenticationService.authenticatedProfile.id;
        this._settingsService.loadAccounts(ownerId);

        mdDialogRef.close();
        this.close(true);
      });
    }
  }

  public close(refreshHistoryItems: boolean): void {
    this._dialogRef.close(refreshHistoryItems);
  }

  public isTypeSelected(type: string): boolean {
    return this.historyItem.type === type;
  }

  public getAccountPlaceholder(): string {
    if (this.isTypeSelected('transfer')) {
      return 'Со счета';
    }

    return 'Счет';
  }

  public showGoalContainer(): boolean {
    return (this.isTypeSelected('expense') || this.isTypeSelected('income')) && this.selectedCategory && this.selectedCategory.length === 2;
  }

  public onSelectedGoalStatusChange(status: boolean): void {
    this._goalStatusChange = status;
  }

  private initNewHistoryItemFromExisting(historyType: string, originalItem: HistoryType): HistoryType {
    const balance: HistoryBalanceType = originalItem.balance;
    return this.initNewHistoryItem(historyType, originalItem.date, balance.value, balance.currency, balance.newCurrency,
      balance.alternativeCurrency, balance.account, balance.subAccount, originalItem.description);
  }

  private initNewHistoryItem(historyType: string, historyDate: number, balanceValue?: number, balanceCurrency?: string, balanceNewCurrency?: string,
                             balanceAlternativeCurrency?: {[key: string]: number}, balanceAccount?: string, balanceSubAccount?: string, historyDescription?: string): HistoryType {
    const result: HistoryType = {
      ownerId: this._authenticationService.authenticatedProfile.id,
      type: historyType,
      date: historyDate,
      description: historyDescription,
      balance: {
        value: balanceValue,
        account: balanceAccount,
        subAccount: balanceSubAccount,
        currency: balanceCurrency || this._currencyService.defaultCurrency.name
      }
    };

    if (historyType === 'expense' || historyType === 'income') {
      result.balance.alternativeCurrency = balanceAlternativeCurrency || this._currencyService.defaultCurrency.conversions;
    }
    if (historyType === 'exchange') {
      result.balance.newCurrency = balanceNewCurrency || this._currencyService.defaultCurrency.name;
    }

    return result;
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
}
