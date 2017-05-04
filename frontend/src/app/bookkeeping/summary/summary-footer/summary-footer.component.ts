import {Component, Input} from '@angular/core';

@Component({
  selector: 'bk-summary-footer',
  templateUrl: './summary-footer.component.html',
  styleUrls: ['./summary-footer.component.css']
})
export class SummaryFooterComponent {
  @Input()
  public summaries: Summary[];
  @Input()
  public conversionCurrency: Currency;
}
