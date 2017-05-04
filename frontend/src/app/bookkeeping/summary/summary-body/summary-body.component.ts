import {Component, Input} from '@angular/core';

@Component({
  selector: 'bk-summary-body',
  templateUrl: './summary-body.component.html',
  styleUrls: ['./summary-body.component.css']
})
export class SummaryBodyComponent {
  @Input()
  public summaries: Summary[];
  @Input()
  public conversionCurrency: Currency;
}
