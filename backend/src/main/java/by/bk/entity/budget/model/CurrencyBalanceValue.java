package by.bk.entity.budget.model;

import by.bk.entity.currency.Currency;
import lombok.Getter;
import lombok.Setter;

/**
 * @author Sergey Koval
 */
@Getter
@Setter
public class CurrencyBalanceValue extends BalanceValue {
    private Currency currency;
}