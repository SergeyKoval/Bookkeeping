import {Component, Input} from '@angular/core';

import {Account} from '../../../common/model/summary/Account';

@Component({
  selector: 'bk-summary-footer',
  templateUrl: './summary-footer.component.html',
  styleUrls: ['./summary-footer.component.css']
})
export class SummaryFooterComponent {
  @Input()
  public accounts: Account[];
  @Input()
  public conversionCurrency: Currency;
}
