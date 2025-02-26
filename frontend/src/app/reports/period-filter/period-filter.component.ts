import { Component, EventEmitter, OnInit, Output } from '@angular/core';
import { MatDateRangeInput, MatDateRangePicker, MatEndDate, MatStartDate } from '@angular/material/datepicker';
import { FormControl, ReactiveFormsModule } from '@angular/forms';
import moment, { Moment } from 'moment/moment';
import { PeriodFilter } from '../../common/model/report/period-filter';

@Component({
  selector: 'bk-period-filter',
  standalone: true,
  imports: [
    MatDateRangeInput,
    MatDateRangePicker,
    MatEndDate,
    MatStartDate,
    ReactiveFormsModule
  ],
  templateUrl: './period-filter.component.html',
  styleUrl: './period-filter.component.css'
})
export class PeriodFilterComponent implements OnInit {
  @Output()
  public updatePeriod: EventEmitter<PeriodFilter> = new EventEmitter<PeriodFilter>();

  public startDate: FormControl<Moment> = new FormControl(moment([moment().year(), moment().month(), 1]));
  public endDate: FormControl<Moment> = new FormControl(moment());
  public period: string;

  public ngOnInit(): void {
    this.onClose();
  }

  public onClose(): void {
    this.period = `${this.startDate.value.format('DD-MM-YYYY')} - ${this.endDate.value.format('DD-MM-YYYY')}`;
    this.updatePeriod.next({
      startDate: {
        year: this.startDate.value.year(),
        month: this.startDate.value.month() + 1,
        day: this.startDate.value.date()
      },
      endDate: {
        year: this.endDate.value.year(),
        month: this.endDate.value.month() + 1,
        day: this.endDate.value.date()
      }});
  }
}
