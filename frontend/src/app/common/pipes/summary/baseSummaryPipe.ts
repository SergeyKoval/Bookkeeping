import { CurrencyService } from '../../service/currency.service';

export abstract class BaseSummaryPipe {
  public constructor(protected _currencyService: CurrencyService) {}

  protected populateBalanceMap(account: FinAccount, balanceMap: Map<string, number>): void {
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

        balanceItems[0].value += currencyService.convertToCurrency(balanceValue, key, currency);
      }
    });

    return balanceItems;
  }
}
