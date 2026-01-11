package by.bk.entity.currency;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collection;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.data.mongodb.core.query.Update;

/**
 * @author Sergey Koval
 */
@Service
public class CurrencyService implements CurrencyAPI {
    @Autowired
    private CurrencyRepository currencyRepository;
    @Autowired
    private MongoTemplate mongoTemplate;

    @Override
    public List<CurrencyDetail> getCurrenciesForDay(LocalDate date) {
        return currencyRepository.getByYearAndMonthAndDay(date.getYear(), date.getMonthValue(), date.getDayOfMonth());
    }

    @Override
    public List<CurrencyDetail> getCurrenciesForMonth(Integer year, Integer month, List<Currency> currencies) {
        Query query = new Query();
        query.addCriteria(Criteria.where("year").is(year).and("month").is(month).and("name").in(currencies));
        query.fields().exclude("id");

        Collection<Currency> projectCurrencies = CollectionUtils.disjunction(Arrays.asList(Currency.values()), currencies);
        projectCurrencies.forEach(currency -> query.fields().exclude(StringUtils.join("conversions.", currency.name())));
        return mongoTemplate.find(query, CurrencyDetail.class);
    }

    @Override
    public CurrencyDetail insert(CurrencyDetail currencyDetail) {
        return currencyRepository.insert(currencyDetail);
    }

    @Override
    public void recalculateMonthlyAverage(int year, int month) {
        var dailyRates = currencyRepository.getByYearAndMonthAndDayIsNotNull(year, month);
        if (dailyRates.isEmpty()) {
            return;
        }

        // Group daily rates by currency
        var ratesByCurrency = dailyRates.stream().collect(Collectors.groupingBy(CurrencyDetail::getName));

        // Calculate and upsert average for each currency
        for (var currency : Currency.values()) {
            var currencyDailyRates = ratesByCurrency.get(currency);
            if (currencyDailyRates == null || currencyDailyRates.isEmpty()) {
                continue;
            }

            var averageConversions = calculateAverageConversions(currencyDailyRates);
            upsertMonthlyAverage(year, month, currency, averageConversions);
        }
    }

    private Map<Currency, Double> calculateAverageConversions(List<CurrencyDetail> dailyRates) {
        var sums = new EnumMap<Currency, Double>(Currency.class);
        var counts = new EnumMap<Currency, Integer>(Currency.class);

        for (var daily : dailyRates) {
            if (daily.getConversions() == null) {
                continue;
            }
            for (var entry : daily.getConversions().entrySet()) {
                sums.merge(entry.getKey(), entry.getValue(), Double::sum);
                counts.merge(entry.getKey(), 1, Integer::sum);
            }
        }

        var averages = new EnumMap<Currency, Double>(Currency.class);
        for (var entry : sums.entrySet()) {
            var key = entry.getKey();
            var average = entry.getValue() / counts.get(key);
            // Round to 4 decimal places
            averages.put(key, Math.round(average * 10000.0) / 10000.0);
        }
        return averages;
    }

    private void upsertMonthlyAverage(int year, int month, Currency currency, Map<Currency, Double> conversions) {
        var query = new Query(Criteria.where("year").is(year)
                .and("month").is(month)
                .and("name").is(currency)
                .and("day").is(null));

        var update = new Update()
                .set("year", year)
                .set("month", month)
                .set("name", currency)
                .set("conversions", conversions);

        mongoTemplate.upsert(query, update, CurrencyDetail.class);
    }
}
