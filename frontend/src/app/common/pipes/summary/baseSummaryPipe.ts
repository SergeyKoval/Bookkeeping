import { CurrencyService } from '../../service/currency.service';
import { FinAccount } from '../../model/fin-account';
import { SubAccount } from '../../model/sub-account';
import { CurrencyDetail } from '../../model/currency-detail';
import { BalanceItem } from '../../model/balance-item';

export abstract class BaseSummaryPipe {
  protected constructor(protected _currencyService: CurrencyService) {}

  protected populateBalanceMap(account: FinAccount, balanceMap: Map<string, number>): void {
    account.subAccounts.forEach((subAccount: SubAccount) => {
      Object.entries(subAccount.balance)
        .filter(([currency, value]) => value !== 0)
        .forEach(([currency, value]) => {
          if (!balanceMap.has(currency)) {
            balanceMap.set(currency, value);
          } else {
            balanceMap.set(currency, balanceMap.get(currency) + value);
          }
      });
    });
  }

  protected calculateBalance(balanceMap: Map<string, number>, currency: CurrencyDetail): BalanceItem[] {
    const balanceItems: BalanceItem[] = [];
    const currencyService: CurrencyService = this._currencyService;
    balanceMap.forEach((balanceValue: number, key: string) => {
      if (!currency) {
        balanceItems.push({currency: key, value: balanceValue});
      } else {
        if (balanceItems.length === 0) {
          balanceItems.push({currency: currency.name, value: 0});
        }

        balanceItems[0].value += currencyService.convertToCurrency(balanceValue, key, currency.name);
      }
    });

    return balanceItems;
  }
}
