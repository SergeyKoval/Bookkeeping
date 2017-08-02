import {Component, Input} from '@angular/core';

@Component({
  selector: 'bk-summary-body',
  templateUrl: './summary-body.component.html',
  styleUrls: ['./summary-body.component.css']
})
export class SummaryBodyComponent {
  @Input()
  public accounts: FinAccount[];
  @Input()
  public conversionCurrency: Currency;

  public changeOpenState(account: FinAccount): void {
    account.opened = !account.opened;
  }
}
