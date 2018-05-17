package by.bk.entity.currency;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author Sergey Koval
 */
@Service
public class CurrencyService implements CurrencyAPI {
    @Autowired
    private CurrencyRepository currencyRepository;

    @Override
    public List<CurrencyDetail> getCurrenciesForDay(LocalDateTime date) {
        return currencyRepository.getByYearAndMonthAndDay(date.getYear(), date.getMonthValue(), date.getDayOfMonth());
    }

    @Override
    public CurrencyDetail insert(CurrencyDetail currencyDetail) {
        return currencyRepository.insert(currencyDetail);
    }
}