import { ChangeDetectionStrategy, Component, Input } from '@angular/core';

import { Account } from '../../common/redux/reducers/user/account.reducer';

@Component({
  selector: 'bk-summary-footer',
  templateUrl: './summary-footer.component.html',
  styleUrls: ['./summary-footer.component.css'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class SummaryFooterComponent {
  @Input()
  public accounts: Array<Account>;
  @Input()
  public conversionCurrency: CurrencyDetail;
}
