package by.bk.controller.model;

import by.bk.entity.currency.Currency;
import lombok.Getter;

import java.util.List;

/**
 * @author Sergey Koval
 */
@Getter
public class MonthCurrenciesRequest {
    private Integer month;
    private Integer year;
    private List<Currency> currencies;
}