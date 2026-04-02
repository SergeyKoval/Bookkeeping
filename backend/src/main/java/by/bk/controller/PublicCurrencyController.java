package by.bk.controller;

import by.bk.entity.currency.Currency;
import by.bk.entity.currency.CurrencyAPI;
import by.bk.entity.currency.CurrencyDetail;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.HashMap;

/**
 * @author Sergey Koval
 */
@RestController
@RequestMapping("/api/public/currencies")
public class PublicCurrencyController {
    @Autowired
    private CurrencyAPI currencyAPI;

    @GetMapping("/{code}/rate")
    public ResponseEntity<CurrencyDetail> getRate(
            @PathVariable String code,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        try {
            var currencyCode = Currency.valueOf(code.toUpperCase());
            var result = currencyAPI.getCurrenciesForDayOrNearest(date);
            var currency = result.stream()
                    .filter(c -> c.getName() == currencyCode)
                    .findFirst();
            return currency.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/rate")
    public ResponseEntity<?> getAllCurrenciesForDate(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        try {
            var result = currencyAPI.getCurrenciesForDayOrNearest(date);
            if (result.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            var firstCurrency = result.get(0);
            var response = new HashMap<>();
            response.put("year", firstCurrency.getYear());
            response.put("month", firstCurrency.getMonth());
            response.put("day", firstCurrency.getDay());

            var rates = new HashMap<>();
            result.forEach(detail -> rates.put(detail.getName(), detail.getConversions()));
            response.put("rates", rates);

            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }
}
