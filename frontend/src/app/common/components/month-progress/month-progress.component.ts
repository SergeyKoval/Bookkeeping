import { Component, Input } from '@angular/core';
import { MonthProgress } from '../../model/budget/month-progress';

@Component({
  selector: 'bk-month-progress',
  templateUrl: './month-progress.component.html',
  styleUrls: ['./month-progress.component.css']
})
export class MonthProgressComponent {
  @Input()
  public height: number;
  @Input()
  public progress: MonthProgress;
}
