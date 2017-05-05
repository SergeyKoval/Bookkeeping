import {Component, Input} from '@angular/core';

import {SummaryCategory} from '../../../common/model/SummaryCategory';

@Component({
  selector: 'bk-summary-body',
  templateUrl: './summary-body.component.html',
  styleUrls: ['./summary-body.component.css']
})
export class SummaryBodyComponent {
  @Input()
  public categories: SummaryCategory[];
  @Input()
  public conversionCurrency: Currency;
}
