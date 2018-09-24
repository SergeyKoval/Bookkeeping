import { Component, ElementRef, Inject, OnInit, ViewChild } from '@angular/core';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material';

import { Observable } from 'rxjs';
import { filter, tap } from 'rxjs/internal/operators';
import { IMyDate, IMyDateModel, IMyDpOptions } from 'mydatepicker';

import { HistoryService } from '../../common/service/history.service';
import { CurrencyService } from '../../common/service/currency.service';
import { ProfileService } from '../../common/service/profile.service';
import { LoadingDialogComponent } from '../../common/components/loading-dialog/loading-dialog.component';
import { LoadingService } from '../../common/service/loading.service';
import { AlertService } from '../../common/service/alert.service';
import { AlertType } from '../../common/model/alert/AlertType';
import { ConfirmDialogService } from '../../common/components/confirm-dialog/confirm-dialog.service';
import { CurrencyValuePipe } from '../../common/pipes/currency-value.pipe';

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
  private _originalGoalDetails: GoalDetails;
  private _goalDetails: GoalDetails;
  private _originalHistoryItem: HistoryType;

  public constructor(
    @Inject(MAT_DIALOG_DATA) public data: {historyItem: HistoryType, editMode: boolean},
    private _dialogRef: MatDialogRef<HistoryEditDialogComponent>,
    private _historyService: HistoryService,
    private _currencyService: CurrencyService,
    private _authenticationService: ProfileService,
    private _profileService: ProfileService,
    private _loadingService: LoadingService,
    private _alertService: AlertService,
    private _confirmDialogService: ConfirmDialogService,
    private _currencyValuePipe: CurrencyValuePipe
  ) {}

  public ngOnInit(): void {
    const today: Date = new Date(Date.now());
    if (this.data.editMode === true && (this.data.historyItem.type === 'expense' || this.data.historyItem.type === 'income')) {
      this._originalHistoryItem = Object.assign({}, this.data.historyItem);
      this._originalHistoryItem.balance = Object.assign({}, this.data.historyItem.balance);
    }
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
        this.loadAlternativeCurrencies(date);
      } else {
        this.updateAlternativeCurrencies();
      }
      this.selectedDate = date;
      this.historyItem.year = date.year;
      this.historyItem.month = date.month;
      this.historyItem.day = date.day;
    }
  }

  public changeCurrency(currency: CurrencyDetail): void {
    if (this.historyItem.balance.currency !== currency.name) {
      this.historyItem.balance.currency = currency.name;
      if (this.historyItem.type === 'expense' || this.historyItem.type === 'income') {
        if (!this._currencyService.isCurrencyHistoryLoaded(this.historyItem.balance.currency, this.selectedDate)) {
          this.alternativeCurrencyLoading = true;
          this.loadAlternativeCurrencies(this.selectedDate);
        } else {
          this.updateAlternativeCurrencies();
        }
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

  public onGoalDetailsChange(goalDetails: GoalDetails): void {
    this._goalDetails = goalDetails;
  }

  public onOriginalGoalDetailsChange(originalGoalDetails: GoalDetails): void {
    this._originalGoalDetails = originalGoalDetails;
  }

  public isTypeSelected(type: string): boolean {
    return this.historyItem.type === type;
  }

  /* tslint:disable:cyclomatic-complexity */
  public save(): void {
    let validationResult: boolean;
    this.errors = null;
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
      if (
        (this.historyItem.type === 'expense' || this.historyItem.type === 'income')
        && this.historyItem.goal
        && !this._goalDetails.done
        && this._goalDetails.changeStatus === false
        && this._goalDetails.percent >= 100
      ) {
        this._confirmDialogService.openConfirmDialog('Изменение статуса цели',
          `Выбранная цель выполнена на ${this._currencyValuePipe.transform(this._goalDetails.percent, 0)}%. Пометить ее выполненной?`)
          .afterClosed()
          .subscribe(result => {
            this._goalDetails.changeStatus = result;
            this.processSaveResult();
          });
      } else if (
        this.data.editMode === true
        && this._originalGoalDetails
        && (this.historyItem.type === 'expense' || this.historyItem.type === 'income')
        && this.historyItem.year === this._originalHistoryItem.year
        && this.historyItem.month === this._originalHistoryItem.month
        && this.historyItem.category === this._originalHistoryItem.category
        && this.historyItem.goal === this._originalHistoryItem.goal
        && this._originalGoalDetails.done === true
        && this._goalDetails.percent < 100
      ) {
        this._confirmDialogService.openConfirmDialog('Изменение статуса цели',
          `После редактирования выбранная цель выполнена на ${this._currencyValuePipe.transform(this._goalDetails.percent, 0)}%. Пометить ее невыполненной?`)
          .afterClosed()
          .subscribe(result => {
            this._goalDetails.changeStatus = result;
            this.processSaveResult();
          });
      } else {
        this.processSaveResult();
      }
    }
  }
  /* tslint:enable */

  public changeNewCurrency(currency: CurrencyDetail): void {
    this.historyItem.balance.newCurrency = currency.name;
  }

  public changeBalanceValue(newBalanceValue: number): void {
    if (newBalanceValue !== this.historyItem.balance.value) {
      this.historyItem.balance.value = newBalanceValue;
      this.updateAlternativeCurrencies();
    }
  }

  public close(refreshHistoryItems: boolean): void {
    this._dialogRef.close(refreshHistoryItems);
  }

  public showGoalContainer(): boolean {
    return (this.isTypeSelected('expense') || this.isTypeSelected('income')) && this.selectedCategory && this.selectedCategory.length === 2;
  }

  private initNewHistoryItem(historyType: string, year: number, month: number, day: number, balanceValue?: number, balanceCurrency?: string, balanceNewCurrency?: string,
                             balanceAlternativeCurrency?: {[key: string]: number}, balanceAccount?: string, balanceSubAccount?: string, historyDescription?: string): HistoryType {

    const currencyName: string = balanceCurrency || this._authenticationService.defaultCurrency.name;
    const result: HistoryType = {
      'type': historyType,
      'year': year,
      'month': month,
      'day': day,
      'description': historyDescription,
      'balance': {
        'value': balanceValue,
        'account': balanceAccount,
        'subAccount': balanceSubAccount,
        'currency': currencyName,
        'alternativeCurrency': balanceAlternativeCurrency
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
    if (!balance.value || balance.value < 0.01 || balance.value >= 10000000) {
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
        this.updateAlternativeCurrencies();
        this.alternativeCurrencyLoading = false;
      });
  }

  private updateAlternativeCurrencies(): void {
    const balance: HistoryBalanceType = this.historyItem.balance;
    balance.alternativeCurrency = {};
    this._profileService.authenticatedProfile.currencies
      .filter(currency => currency.name !== balance.currency)
      .forEach(currency => {
        const alternativeValue: number = this._currencyService.convertToCurrency(balance.value, balance.currency, currency.name, this.selectedDate);
        balance.alternativeCurrency[currency.name] = Number(alternativeValue.toFixed(2));
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

    if (!balance.newValue || balance.newValue < 0.01 || balance.newValue >= 10000000) {
      this.errors = 'Сумма указана неверно';
      return false;
    }

    if (balance.currency === balance.newCurrency) {
      this.errors = 'Валюты не могут совпадать';
      return false;
    }

    return true;
  }

  private processSaveResult(): void {
    const mdDialogRef: MatDialogRef<LoadingDialogComponent> = this._loadingService.openLoadingDialog('Сохранение...');
    const originalGoalStatusChanged: boolean = this._originalGoalDetails ? this._originalGoalDetails.changeStatus : false;
    const goalStatusChanged: boolean = this._goalDetails ? this._goalDetails.changeStatus : false;
    const result: Observable<SimpleResponse> = this.data.editMode
      ? this._historyService.editHistoryItem(this.historyItem, goalStatusChanged, originalGoalStatusChanged)
      : this._historyService.addHistoryItem(this.historyItem, goalStatusChanged);
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
      this._alertService.addAlert(AlertType.SUCCESS, 'Операция успешно учтена');
      this.close(true);
    });
  }
}
