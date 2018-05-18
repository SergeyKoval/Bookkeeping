package by.bk.controller;

import by.bk.controller.model.CurrencyResponse;
import by.bk.controller.model.MonthCurrenciesRequest;
import by.bk.entity.currency.Currency;
import by.bk.entity.currency.CurrencyAPI;
import by.bk.entity.currency.CurrencyDetail;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

    @GetMapping("/default-currencies")
    public List<CurrencyResponse> getDefaultCurrencies() {
        return Stream.of(Currency.values()).map(CurrencyResponse::new).collect(Collectors.toList());
    }
}