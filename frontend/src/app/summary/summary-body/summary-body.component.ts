import { Component, Input } from '@angular/core';

import { ProfileService } from '../../common/service/profile.service';
import { FinAccount } from '../../common/model/fin-account';
import { CurrencyDetail } from '../../common/model/currency-detail';

@Component({
  selector: 'bk-summary-body',
  templateUrl: './summary-body.component.html',
  styleUrls: ['./summary-body.component.css']
})
export class SummaryBodyComponent {
  @Input()
  public accounts: FinAccount[];
  @Input()
  public conversionCurrency: CurrencyDetail;

  public constructor(private _profileService: ProfileService) {}

  public changeOpenState(account: FinAccount): void {
    account.opened = !account.opened;
    this._profileService.toggleAccount(account.title, account.opened);
  }
}
