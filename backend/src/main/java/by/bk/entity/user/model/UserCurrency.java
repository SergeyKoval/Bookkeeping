package by.bk.entity.user.model;

import by.bk.entity.currency.Currency;
import lombok.Getter;

/**
 * @author Sergey Koval
 */
@Getter
public class UserCurrency {
    private Currency name;
    private boolean defaultCurrency;
    private int order;

    public UserCurrency() {
    }

    public UserCurrency(Currency name, boolean defaultCurrency, int order) {
        this.name = name;
        this.defaultCurrency = defaultCurrency;
        this.order = order;
    }

    public String getSymbol() {
        return name.getSymbol();
    }
}