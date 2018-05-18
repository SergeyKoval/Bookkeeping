package by.bk.entity.currency.job;

import by.bk.entity.currency.Currency;
import by.bk.entity.currency.CurrencyAPI;
import by.bk.entity.currency.CurrencyDetail;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.AbstractMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Sergey Koval
 */
@Component
public class NbrbJob {
    private static final Log LOG = LogFactory.getLog(NbrbJob.class);
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
    private static final String USD_URL = "http://www.nbrb.by/API/ExRates/Rates/USD?ParamMode=2";
    private static final String EUR_URL = "http://www.nbrb.by/API/ExRates/Rates/EUR?ParamMode=2";
    private static final String RUB_URL = "http://www.nbrb.by/API/ExRates/Rates/RUB?ParamMode=2";
    private static final DecimalFormat CURRENCY_FORMAT = new DecimalFormat("##0.0000");
    private static final BiFunction<Currency, Double, Map.Entry<Currency, Double>> CONVERSION_ENTRY =
            (currency, value) -> new AbstractMap.SimpleEntry<>(currency, Double.parseDouble(CURRENCY_FORMAT.format(value)));

    @Autowired
    private CurrencyAPI currencyAPI;
    @Autowired
    private RestTemplate restTemplate;

    @Scheduled(fixedDelay = 3600000)
    public void getFreshCurrencies() {
        LocalDate today = LocalDate.now();
        List<CurrencyDetail> todayCurrencies = currencyAPI.getCurrenciesForDay(today);
        if (todayCurrencies.isEmpty()) {
            NbrbCurrency eurCurrency = restTemplate.getForObject(EUR_URL, NbrbCurrency.class);
            NbrbCurrency usdCurrency = restTemplate.getForObject(USD_URL, NbrbCurrency.class);
            NbrbCurrency rubCurrency = restTemplate.getForObject(RUB_URL, NbrbCurrency.class);

            if (dateValidation(today, eurCurrency, usdCurrency, rubCurrency)) {
                Map<Currency, Double> conversions = Stream.of(
                        CONVERSION_ENTRY.apply(Currency.USD, (1 / usdCurrency.getOfficialRate())),
                        CONVERSION_ENTRY.apply(Currency.EUR, (1 / eurCurrency.getOfficialRate())),
                        CONVERSION_ENTRY.apply(Currency.RUB, (1 / rubCurrency.getOfficialRate() / 100))
                ).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
                addCurrencyDetail(today, Currency.BYN, conversions);

                conversions = Stream.of(
                        CONVERSION_ENTRY.apply(Currency.BYN, usdCurrency.getOfficialRate()),
                        CONVERSION_ENTRY.apply(Currency.EUR, (usdCurrency.getOfficialRate() / eurCurrency.getOfficialRate())),
                        CONVERSION_ENTRY.apply(Currency.RUB, (usdCurrency.getOfficialRate() / rubCurrency.getOfficialRate() * 100))
                ).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
                addCurrencyDetail(today, Currency.USD, conversions);

                conversions = Stream.of(
                        CONVERSION_ENTRY.apply(Currency.BYN, eurCurrency.getOfficialRate()),
                        CONVERSION_ENTRY.apply(Currency.USD, (eurCurrency.getOfficialRate() / usdCurrency.getOfficialRate())),
                        CONVERSION_ENTRY.apply(Currency.RUB, (eurCurrency.getOfficialRate() / rubCurrency.getOfficialRate() * 100))
                ).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
                addCurrencyDetail(today, Currency.EUR, conversions);

                conversions = Stream.of(
                        CONVERSION_ENTRY.apply(Currency.BYN, (rubCurrency.getOfficialRate() / 100)),
                        CONVERSION_ENTRY.apply(Currency.USD, (rubCurrency.getOfficialRate() / 100 / usdCurrency.getOfficialRate())),
                        CONVERSION_ENTRY.apply(Currency.EUR, (rubCurrency.getOfficialRate() / 100 / eurCurrency.getOfficialRate()))
                ).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
                addCurrencyDetail(today, Currency.RUB, conversions);
            }
        }
    }

    private void addCurrencyDetail(LocalDate today, Currency currency, Map<Currency, Double> conversions) {
        CurrencyDetail currencyDetail = new CurrencyDetail();
        currencyDetail.setName(currency);
        currencyDetail.setYear(today.getYear());
        currencyDetail.setMonth(today.getMonthValue());
        currencyDetail.setDay(today.getDayOfMonth());
        currencyDetail.setConversions(conversions);
        currencyAPI.insert(currencyDetail);
    }

    private boolean dateValidation(LocalDate today, NbrbCurrency eurCurrency, NbrbCurrency usdCurrency, NbrbCurrency rubCurrency) {
        LocalDate eurDate = LocalDate.parse(eurCurrency.getDate(), DATE_FORMAT);
        if (!today.isEqual(eurDate)) {
            LOG.warn(StringUtils.join("EUR nbrb currency are for ", eurDate, " but expecting ", today));
            return false;
        }

        LocalDate usdDate = LocalDate.parse(usdCurrency.getDate(), DATE_FORMAT);
        if (!today.isEqual(usdDate)) {
            LOG.warn(StringUtils.join("USD nbrb currency are for ", usdDate, " but expecting ", today));
            return false;
        }

        LocalDate rubDate = LocalDate.parse(rubCurrency.getDate(), DATE_FORMAT);
        if (!today.isEqual(rubDate)) {
            LOG.warn(StringUtils.join("RUB nbrb currency are for ", rubDate, " but expecting ", today));
            return false;
        }

        return true;
    }
}