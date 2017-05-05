import {Component, Input} from '@angular/core';

import {Category} from '../../../common/model/summary/Category';

@Component({
  selector: 'bk-summary-body',
  templateUrl: './summary-body.component.html',
  styleUrls: ['./summary-body.component.css']
})
export class SummaryBodyComponent {
  @Input()
  public categories: Category[];
  @Input()
  public conversionCurrency: Currency;
}
