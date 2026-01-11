package by.bk.entity.currency;

import java.time.LocalDate;
import java.util.List;

/**
 * @author Sergey Koval
 */
public interface CurrencyAPI {
    List<CurrencyDetail> getCurrenciesForDay(LocalDate date);
    List<CurrencyDetail> getCurrenciesForMonth(Integer year, Integer month, List<Currency> currencies);
    CurrencyDetail insert(CurrencyDetail currencyDetail);
    void recalculateMonthlyAverage(int year, int month);
}