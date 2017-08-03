import {Component, ElementRef, Inject, OnInit, ViewChild} from '@angular/core';
import {FormGroup} from '@angular/forms';
import {MD_DIALOG_DATA, MdDialogRef} from '@angular/material';

import {IMyDate, IMyDateModel, IMyDpOptions} from 'mydatepicker';
import {HistoryService} from '../../../common/service/history.service';
import {CurrencyService} from '../../../common/service/currency.service';
import {SettingsService} from '../../../common/service/settings.service';

@Component({
  selector: 'bk-history-edit-popup',
  templateUrl: './history-edit-popup.component.html',
  styleUrls: ['./history-edit-popup.component.css']
})
export class HistoryEditPopupComponent implements OnInit {
  public currencies: Currency[];
  public selectedDate: IMyDate;
  public historyForm: FormGroup;
  public accounts: SelectItem[];
  public categories: SelectItem[];
  public selectedAccount: SelectItem[];
  public selectedToAccount: SelectItem[];
  public selectedCategory: SelectItem[];
  public datePickerOptions: IMyDpOptions = {
    dateFormat: 'dd.mm.yyyy',
    inline: true
  };

  @ViewChild('title')
  private _titleElement: ElementRef;

  public constructor(
    public dialogRef: MdDialogRef<HistoryEditPopupComponent>,
    @Inject(MD_DIALOG_DATA) public data: {title: string, historyItem: HistoryType},
    private _dialogRef: MdDialogRef<HistoryEditPopupComponent>,
    private _historyService: HistoryService,
    private _currencyService: CurrencyService,
    private _settingsService: SettingsService
  ) {}

  public ngOnInit(): void {
    this._currencyService.currencies$.subscribe((currencies: Currency[]) => this.currencies = currencies);
    this._settingsService.accounts$.subscribe((accounts: FinAccount[]) => this.accounts = this._settingsService.transformAccounts(accounts));
    this._settingsService.categories$.subscribe((catefories: Category[]) => this.categories = this._settingsService.transformCategories(catefories));

    const date: Date = new Date();
    const historyType: string = this.data.historyItem.type;
    date.setTime(this.data.historyItem.date);
    this.selectedDate = {year: date.getFullYear(), month: date.getMonth() + 1, day: date.getDate()};
    this.historyForm = this._historyService.initHistoryForm(this.data.historyItem);
  }

  public onDateChanged(event: IMyDateModel): void {
    this._titleElement.nativeElement.click();
    console.log(event.date);
    // Update value of selDate variable
    this.selectedDate = event.date;
  }

  public onChangeSelectedType(type: string): void {
    if (!this.isTypeSelected(type)) {
      this.historyForm.get('type').setValue(type);
    }
  }

  public openCurrenciesPopup(): void {

  }

  public chooseCurrency(currency: Currency): void {
    // TODO: update goal
    this.historyForm.get('balance').get('currency').setValue(currency.name);
  }

  public save(): void {
    console.log(this.historyForm.value);
    this.close();
  }

  public close(): void {
    this._dialogRef.close();
  }

  public isTypeSelected(type: string): boolean {
    return this.historyForm.get('type').value === type;
  }
}
