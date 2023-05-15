import { Component, Input } from '@angular/core';
import { FinAccount } from '../../common/model/fin-account';
import { CurrencyDetail } from '../../common/model/currency-detail';

@Component({
  selector: 'bk-summary-footer',
  templateUrl: './summary-footer.component.html',
  styleUrls: ['./summary-footer.component.css']
})
export class SummaryFooterComponent {
  @Input()
  public accounts: FinAccount[];
  @Input()
  public conversionCurrency: CurrencyDetail;
}
