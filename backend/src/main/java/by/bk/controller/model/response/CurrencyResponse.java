package by.bk.controller.model.response;

import by.bk.entity.currency.Currency;
import lombok.Getter;

/**
 * @author Sergey Koval
 */
@Getter
public class CurrencyResponse {
    private String name;
    private String symbol;

    public CurrencyResponse(Currency currency) {
        this.name = currency.name();
        this.symbol = currency.getSymbol();
    }
}