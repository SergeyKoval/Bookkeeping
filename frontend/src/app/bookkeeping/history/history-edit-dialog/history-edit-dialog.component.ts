import {Component, ElementRef, Inject, OnInit, ViewChild} from '@angular/core';
import {Response} from '@angular/http';
import {MD_DIALOG_DATA, MdDialog, MdDialogRef} from '@angular/material';

import {IMyDate, IMyDateModel, IMyDpOptions} from 'mydatepicker';
import {HistoryService} from '../../../common/service/history.service';
import {CurrencyService} from '../../../common/service/currency.service';
import {SettingsService} from '../../../common/service/settings.service';
import {DateUtilsService} from '../../../common/utils/date-utils.service';
import {AuthenticationService} from '../../../common/service/authentication.service';
import {LoadingDialogComponent} from '../../../common/components/loading-dialog/loading-dialog.component';
import {LoadingService} from '../../../common/service/loading.service';
import {AlertService} from '../../../common/service/alert.service';
import {AlertType} from '../../../common/model/alert/AlertType';
import {Alert} from '../../../common/model/alert/Alert';
import {AlternativeCurrenciesDialogComponent} from './alternative-currencies-dialog/alternative-currencies-dialog.component';

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

  public constructor(
    public dialogRef: MdDialogRef<HistoryEditDialogComponent>,
    @Inject(MD_DIALOG_DATA) public data: {title: string, historyItem: HistoryType},
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
    this.historyItem = this.data.historyItem || this.initNewHistoryItem('expense', DateUtilsService.getUTCDate());
    this.selectedDate = DateUtilsService.getDateFromUTC(this.historyItem.date);

    this._currencyService.currencies$.subscribe((currencies: Currency[]) => this.currencies = currencies);
    this._settingsService.accounts$.subscribe((accounts: FinAccount[]) => this.accounts = this._settingsService.transformAccounts(accounts));
    this._settingsService.categories$.subscribe((categories: Category[]) => {
      this._allCategories = categories;
      this.categories = this._settingsService.transformCategories(categories, this.historyItem.type);
    });
  }

  public onDateChanged(event: IMyDateModel): void {
    // TODO: update goal
    this._titleElement.nativeElement.click();
    this.selectedDate = event.date;
    this.historyItem.date = DateUtilsService.getUTCDateByDay(this.selectedDate);
  }

  public onChangeSelectedType(type: string): void {
    if (!this.isTypeSelected(type)) {
      if (this.selectedCategory) {
        this.selectedCategory.length = 0;
      }

      this.historyItem = this.initNewHistoryItemFromExisting(type, this.historyItem);
      this.categories = this._settingsService.transformCategories(this._allCategories, this.historyItem.type);
    }
  }

  public openCurrenciesPopup(): void {
    this._dialog.open(AlternativeCurrenciesDialogComponent, {
      disableClose: true,
      width: '470px',
      data: {
        balance: this.historyItem.balance
      }
    });
  }

  public chooseCurrency(currency: Currency): void {
    // TODO: update goal
    this.historyItem.balance.currency = currency.name;
    this.historyItem.balance.alternativeCurrency = currency.conversions;
  }

  public save(): void {
    let saveResult: boolean;
    switch (this.historyItem.type) {
      case 'expense':
      case 'income':
        saveResult = this.saveExpenseOrIncome();
        break;
    }

    if (saveResult) {
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

  private initNewHistoryItemFromExisting(historyType: string, originalItem: HistoryType): HistoryType {
    const balance: HistoryBalanceType = originalItem.balance;
    return this.initNewHistoryItem(historyType, originalItem.date, balance.value, balance.currency, balance.alternativeCurrency, balance.account, balance.subAccount, originalItem.description);
  }

  private initNewHistoryItem(historyType: string, historyDate: number, balanceValue?: number, balanceCurrency?: string,
                             balanceAlternativeCurrency?: {[key: string]: number}, balanceAccount?: string, balanceSubAccount?: string, historyDescription?: string): HistoryType {
    return {
      ownerId: this._authenticationService.authenticatedProfile.id,
      type: historyType,
      date: historyDate,
      description: historyDescription,
      balance: {
        value: balanceValue,
        account: balanceAccount,
        subAccount: balanceSubAccount,
        currency: balanceCurrency || this._currencyService.defaultCurrency.name,
        alternativeCurrency: balanceAlternativeCurrency || this._currencyService.defaultCurrency.conversions
      }
    };
  }

  private saveExpenseOrIncome(): boolean {
    const balance: HistoryBalanceType = this.historyItem.balance;
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

    if (!this.selectedCategory || this.selectedCategory.length < 2) {
      this.errors = 'Категория не выбрана';
      return false;
    }
    this.historyItem.category = this.selectedCategory[0].title;
    this.historyItem.subCategory = this.selectedCategory[1].title;

    return true;
  }
}
