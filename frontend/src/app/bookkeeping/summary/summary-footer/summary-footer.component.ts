import {Component, Input} from '@angular/core';

import {SummaryCategory} from '../../../common/model/SummaryCategory';

@Component({
  selector: 'bk-summary-footer',
  templateUrl: './summary-footer.component.html',
  styleUrls: ['./summary-footer.component.css']
})
export class SummaryFooterComponent {
  @Input()
  public categories: SummaryCategory[];
  @Input()
  public conversionCurrency: Currency;
}
