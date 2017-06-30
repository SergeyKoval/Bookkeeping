import {Component, Input} from '@angular/core';

import {Account} from '../../../common/model/summary/Account';

@Component({
  selector: 'bk-summary-body',
  templateUrl: './summary-body.component.html',
  styleUrls: ['./summary-body.component.css']
})
export class SummaryBodyComponent {
  @Input()
  public accounts: Account[];
  @Input()
  public conversionCurrency: Currency;

  public changeOpenState(account: Account): void {
    account.opened = !account.opened;
  }
}
