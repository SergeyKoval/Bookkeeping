package by.bk.controller;

import by.bk.controller.model.MonthCurrenciesRequest;
import by.bk.entity.currency.CurrencyAPI;
import by.bk.entity.currency.CurrencyDetail;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @author Sergey Koval
 */
@RestController
@RequestMapping("/api/currency")
public class CurrencyController extends BaseAPIController {
    @Autowired
    private CurrencyAPI currencyAPI;

    @PostMapping("/month-currencies")
    public List<CurrencyDetail> getMonthCurrencies(@RequestBody MonthCurrenciesRequest currenciesRequest) {
        return currencyAPI.getCurrenciesForMonth(currenciesRequest.getYear(), currenciesRequest.getMonth(), currenciesRequest.getCurrencies());
    }
}