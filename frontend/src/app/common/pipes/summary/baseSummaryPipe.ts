import {SubAccount} from '../../model/summary/SubAccount';
import {BalanceItem} from '../../model/summary/BalanceItem';
import {CurrencyService} from '../../service/currency.service';
import {Account} from '../../model/summary/Account';

export abstract class BaseSummaryPipe {
  protected populateBalanceMap(account: Account, balanceMap: Map<string, number>): void {
    account.subAccounts.forEach((subAccount: SubAccount) => {
      subAccount.balance.forEach((balance: BalanceItem) => {
        const balanceCurrency: string = balance.currency;
        if (!balanceMap.has(balanceCurrency)) {
          balanceMap.set(balanceCurrency, balance.value);
        } else {
          balanceMap.set(balanceCurrency, balanceMap.get(balanceCurrency) + balance.value);
        }
      });
    });
  }

  protected calculateBalance(balanceMap: Map<string, number>, currency: Currency): BalanceItem[] {
    const balanceItems: BalanceItem[] = [];
    balanceMap.forEach((value: number, key: string) => {
      if (!currency) {
        balanceItems.push(new BalanceItem(key, value));
      } else {
        if (balanceItems.length === 0) {
          balanceItems.push(new BalanceItem(currency.name, 0));
        }

        balanceItems[0].value += CurrencyService.convertToCurrency(value, key, currency);
      }
    });

    return balanceItems;
  }
}
