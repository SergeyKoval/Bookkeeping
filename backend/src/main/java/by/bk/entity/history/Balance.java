package by.bk.entity.history;

import by.bk.entity.currency.Currency;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.Map;

/**
 * @author Sergey Koval
 */
@Getter
@Setter
@ToString
@NoArgsConstructor
public class Balance {
    private Double value;
    private Double newValue;
    private String account;
    private String accountTo;
    private String subAccount;
    private String subAccountTo;
    private Currency currency;
    private Currency newCurrency;
    private Map<Currency, Double> alternativeCurrency;

    public Balance(String account, String subAccount) {
        this.account = account;
        this.subAccount = subAccount;
    }
}
