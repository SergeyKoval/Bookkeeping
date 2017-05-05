import {Component, Input} from '@angular/core';

import {Category} from '../../../common/model/summary/Category';

@Component({
  selector: 'bk-summary-footer',
  templateUrl: './summary-footer.component.html',
  styleUrls: ['./summary-footer.component.css']
})
export class SummaryFooterComponent {
  @Input()
  public categories: Category[];
  @Input()
  public conversionCurrency: Currency;
}
