package by.bk.entity.currency;

/**
 * @author Sergey Koval
 */
public enum Currency {
    BYN("Byn"),
    USD("&#x24;"),
    EUR("&euro;"),
    RUB("&#8381;");

    private final String symbol;

    Currency(String symbol) {
        this.symbol = symbol;
    }

    public String getSymbol() {
        return symbol;
    }
}