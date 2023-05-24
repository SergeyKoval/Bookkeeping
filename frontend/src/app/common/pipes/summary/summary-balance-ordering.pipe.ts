import { Pipe, PipeTransform } from '@angular/core';

import { ProfileService } from '../../service/profile.service';
import { BalanceItem } from '../../model/balance-item';

@Pipe({
  name: 'summaryBalanceOrdering'
})
export class SummaryBalanceOrderingPipe implements PipeTransform {
  public constructor(private _profileService: ProfileService) {}

  public transform(items: BalanceItem[]): BalanceItem[] {
    if (items.length < 2) {
      return items;
    }

    return this._profileService.sortSummaryBalanceItems(items);
  }
}
