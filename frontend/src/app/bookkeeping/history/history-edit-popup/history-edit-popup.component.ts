import {Component, Inject, OnInit} from '@angular/core';
import {FormGroup} from '@angular/forms';
import {MD_DIALOG_DATA, MdDialogRef, MdTabChangeEvent} from '@angular/material';

import {IMyDate, IMyDateModel, IMyDpOptions} from 'mydatepicker';
import {HistoryService} from '../../../common/service/history.service';
import {CurrencyService} from '../../../common/service/currency.service';

@Component({
  selector: 'bk-history-edit-popup',
  templateUrl: './history-edit-popup.component.html',
  styleUrls: ['./history-edit-popup.component.css']
})
export class HistoryEditPopupComponent implements OnInit {
  public currencies: Currency[];
  public showGoalSection: boolean;
  public selectedDate: IMyDate;
  public historyForm: FormGroup;
  public datePickerOptions: IMyDpOptions = {
    dateFormat: 'dd.mm.yyyy',
    inline: true,
  };

  public constructor(
    public dialogRef: MdDialogRef<HistoryEditPopupComponent>,
    @Inject(MD_DIALOG_DATA) public data: {title: string, historyItem: HistoryType},
    private _dialogRef: MdDialogRef<HistoryEditPopupComponent>,
    private _historyService: HistoryService,
    private _currencyService: CurrencyService
  ) {}

  public ngOnInit(): void {
    this._currencyService.currencies$.subscribe((currencies: Currency[]) => {
      this.currencies = currencies;
    });

    const date: Date = new Date();
    const historyType: string = this.data.historyItem.type;
    date.setTime(this.data.historyItem.date);
    this.selectedDate = {year: date.getFullYear(), month: date.getMonth() + 1, day: date.getDate()};
    this.historyForm = this._historyService.initHistoryForm(this.data.historyItem);
    this.showGoalSection = !historyType || historyType === 'expense' || historyType === 'income';
  }

  public onDateChanged(event: IMyDateModel): void {
    console.log(event.date);
    // Update value of selDate variable
    this.selectedDate = event.date;
  }

  public getSelectedTypeIndex(): number {
    switch (this.historyForm.get('type').value) {
      case 'income': return 1;
      case 'transfer': return 2;
      case 'exchange': return 3;
      case 'expense':
      default: return 0;
    }
  }

  public onChangeSelectedType(event: MdTabChangeEvent): void {
    switch (event.index) {
      case 0:
      case 1: {
        this.showGoalSection = true;
        break;
      }
      case 2:
      case 3: {
        this.showGoalSection = false;
        break;
      }
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
}
