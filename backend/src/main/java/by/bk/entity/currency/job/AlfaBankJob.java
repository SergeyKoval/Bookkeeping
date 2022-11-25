package by.bk.entity.currency.job;

import by.bk.entity.currency.Currency;
import by.bk.entity.currency.CurrencyAPI;
import by.bk.entity.currency.CurrencyDetail;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class AlfaBankJob {
  private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd.MM.yyyy");
  private static final String CURRENCIES_URL = "https://developerhub.alfabank.by:8273/partner/1.0.0/public/nationalRates?date=%s&currencyCode=840&currencyCode=978&currencyCode=643";

  @Autowired
  private CurrencyAPI currencyAPI;
  @Autowired
  private RestTemplate restTemplate;

  @Scheduled(fixedDelay = 3600000)
  public void getFreshCurrencies() {
    LocalDate today = LocalDate.now();
    List<CurrencyDetail> todayCurrencies = currencyAPI.getCurrenciesForDay(today);
    if (todayCurrencies.isEmpty()) {
      AlfaBankCurrencyWrapper currenciesWrapper = restTemplate.getForObject(String.format(CURRENCIES_URL, today.format(DATE_FORMAT)), AlfaBankCurrencyWrapper.class);
      if (CollectionUtils.isEmpty(currenciesWrapper.getRates())) {
        return;
      }

      Map<Currency, Double> conversions = currenciesWrapper.getRates().stream().collect(Collectors.toMap(
          currency -> Currency.valueOf(currency.getIso()),
          currency -> currency.getRate() / currency.getQuantity()));

      Map<Currency, Double> bynConversions = Map.of(
          Currency.USD, (1 / conversions.get(Currency.USD)),
          Currency.EUR, (1 / conversions.get(Currency.EUR)),
          Currency.RUB, (1 / conversions.get(Currency.RUB))
      );
      addCurrencyDetail(today, Currency.BYN, bynConversions);

      Map<Currency, Double> usdConversions = Map.of(
          Currency.BYN, conversions.get(Currency.USD),
          Currency.EUR, (conversions.get(Currency.USD) / conversions.get(Currency.EUR)),
          Currency.RUB, (conversions.get(Currency.USD) / conversions.get(Currency.RUB))
      );
      addCurrencyDetail(today, Currency.USD, usdConversions);

      Map<Currency, Double> eurConversions = Map.of(
          Currency.BYN, conversions.get(Currency.EUR),
          Currency.USD, (conversions.get(Currency.EUR) / conversions.get(Currency.USD)),
          Currency.RUB, (conversions.get(Currency.EUR) / conversions.get(Currency.RUB))
      );
      addCurrencyDetail(today, Currency.EUR, eurConversions);

      Map<Currency, Double> rubConversions = Map.of(
          Currency.BYN, (conversions.get(Currency.RUB)),
          Currency.USD, (conversions.get(Currency.RUB) / conversions.get(Currency.USD)),
          Currency.EUR, (conversions.get(Currency.RUB) / conversions.get(Currency.EUR))
      );
      addCurrencyDetail(today, Currency.RUB, rubConversions);
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

}
