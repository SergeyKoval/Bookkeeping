package by.bk.entity.currency;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author Sergey Koval
 */
public interface CurrencyAPI {
    List<CurrencyDetail> getCurrenciesForDay(LocalDateTime date);
    CurrencyDetail insert(CurrencyDetail currencyDetail);
}