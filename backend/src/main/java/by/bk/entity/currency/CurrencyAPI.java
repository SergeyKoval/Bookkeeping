package by.bk.entity.currency;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author Sergey Koval
 */
public interface CurrencyAPI {
    List<CurrencyDetail> getCurrenciesForDay(LocalDateTime date);
    List<CurrencyDetail> getCurrenciesForMonth(Integer year, Integer month, List<Currency> currencies);
    CurrencyDetail insert(CurrencyDetail currencyDetail);
}