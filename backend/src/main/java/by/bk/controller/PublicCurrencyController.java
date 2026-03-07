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
import java.util.List;

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
}
