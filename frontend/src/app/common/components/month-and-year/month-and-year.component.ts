import { Component, Input, Output } from '@angular/core';

import { Subject } from 'rxjs';

import { DateUtils } from '../../utils/date-utils';

@Component({
  selector: 'bk-month-and-year',
  templateUrl: './month-and-year.component.html',
  styleUrls: ['./month-and-year.component.css']
})
export class MonthAndYearComponent {
  public months: string[] = DateUtils.MONTHS;

  @Input()
  public disabled: boolean = false;
  @Input()
  public selectedYear: number;
  @Input()
  public set selectedMonth(value: number) {
    if (value && !this._selectedMonth) {
      this._selectedMonth = value - 1;
    }
  }

  @Output()
  public changeMonth: Subject<number> = new Subject();
  @Output()
  public changeYear: Subject<{year: number, month: number}> = new Subject();

  private _selectedMonth: number;

  public constructor() { }

  public chooseMonth(monthIndex: number): void {
    if (this._selectedMonth !== monthIndex) {
      this._selectedMonth = monthIndex;
      this.changeMonth.next(this._selectedMonth + 1);
    }
  }

  public getSelectedMonthTitle(): string {
    return this.months[this._selectedMonth];
  }

  public decreaseYear(): void {
    this._selectedMonth = 11;
    this.selectedYear--;
    this.changeYear.next({year: this.selectedYear, month: this._selectedMonth + 1});
  }

  public increaseYear(): void {
    this._selectedMonth = 0;
    this.selectedYear++;
    this.changeYear.next({year: this.selectedYear, month: this._selectedMonth + 1});
  }

  public get selectedMonth(): number {
    return this._selectedMonth;
  }
}
