package by.bk.entity.currency;

/**
 * @author Sergey Koval
 */
public enum Currency {
    BYN("Byn"),
    USD("&#36;"),
    EUR("&#8364;"),
    RUB("&#8381;");

    private final String symbol;

    Currency(String symbol) {
        this.symbol = symbol;
    }

    public String getSymbol() {
        return symbol;
    }
}