package by.bk.entity.budget.model;

import by.bk.entity.currency.Currency;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * @author Sergey Koval
 */
@Getter
@Setter
@NoArgsConstructor
public class CurrencyBalanceValue extends BalanceValue {
    private Currency currency;

    public CurrencyBalanceValue(Double value, Double completeValue, Currency currency) {
        super(value, completeValue);
        this.currency = currency;
    }

    public CurrencyBalanceValue(Currency currency, BalanceValue balanceValue) {
        super(balanceValue.getValue(), balanceValue.getCompleteValue());
        this.currency = currency;
    }
}