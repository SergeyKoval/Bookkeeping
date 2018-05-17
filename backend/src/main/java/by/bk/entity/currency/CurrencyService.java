package by.bk.entity.currency;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

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
    public List<CurrencyDetail> getCurrenciesForDay(LocalDateTime date) {
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
}