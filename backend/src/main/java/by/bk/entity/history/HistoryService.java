package by.bk.entity.history;

import by.bk.controller.model.response.SimpleResponse;
import by.bk.entity.currency.Currency;
import by.bk.entity.currency.CurrencyDetail;
import by.bk.entity.currency.CurrencyRepository;
import by.bk.entity.user.UserAPI;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Sergey Koval
 */
@Service
public class HistoryService implements HistoryAPI {
    private static final Log LOG = LogFactory.getLog(HistoryService.class);

    @Autowired
    private HistoryRepository historyRepository;
    @Autowired
    private CurrencyRepository currencyRepository;
    @Autowired
    private MongoTemplate mongoTemplate;
    @Autowired
    private UserAPI userAPI;

    @Override
    public List<HistoryItem> getPagePortion(String login, int page, int limit) {
        Query query = Query.query(Criteria.where("user").is(login))
                .with(Sort.by(Sort.Order.desc("year"), Sort.Order.desc("month"), Sort.Order.desc("day")))
                .skip((page -1) * limit)
                .limit(limit);

        return mongoTemplate.find(query, HistoryItem.class);
    }

    @Override
    public SimpleResponse addHistoryItem(String login, HistoryItem historyItem) {
        addUnusedCurrencyConversions(historyItem);
        int order = 1 + historyRepository.getAllDayHistoryItemsWithOrder(login, historyItem.getYear(), historyItem.getMonth(), historyItem.getDay())
                .stream()
                .max(Comparator.comparingInt(HistoryItem::getOrder))
                .map(HistoryItem::getOrder)
                .orElse(0);

        historyItem.setOrder(order);
        historyItem.setUser(login);
        HistoryItem savedHistoryItem = historyRepository.save(historyItem);

        if (StringUtils.isBlank(savedHistoryItem.getId())) {
            LOG.error(StringUtils.join("Error adding history item ", savedHistoryItem, " for user ", login));
            return SimpleResponse.fail("ERROR");
        }

        SimpleResponse response = userAPI.updateUserBalance(login, savedHistoryItem.getType(), savedHistoryItem.getBalance());
        if (!response.isSuccess()) {
            LOG.error(StringUtils.join("Error updating user balance based on the history item ", savedHistoryItem, " for user ", login));
            historyRepository.delete(savedHistoryItem);
        }

        return response;
    }

    @Override
    public SimpleResponse editHistoryItem(String login, HistoryItem historyItem) {
        HistoryItem originalHistoryItem = historyRepository.findById(historyItem.getId()).get();
        SimpleResponse response = revertBalanceChange(login, originalHistoryItem.getType(), originalHistoryItem.cloneBalance());
        if (response.isSuccess()) {
            historyItem.setUser(login);
            historyItem = historyRepository.save(historyItem);
            response = userAPI.updateUserBalance(login, historyItem.getType(), historyItem.getBalance());

            if (!response.isSuccess()) {
                LOG.error(StringUtils.join("Error on second updating balance for changing history item. Step 1 success update, which need to be reverted: ", originalHistoryItem));
                historyRepository.save(originalHistoryItem);
                userAPI.updateUserBalance(login, historyItem.getType(), originalHistoryItem.getBalance());
            }
        }

        return SimpleResponse.success();
    }

    @Override
    public SimpleResponse deleteHistoryItem(String login, String historyItemId) {
        HistoryItem originalHistoryItem = historyRepository.findById(historyItemId).get();
        SimpleResponse response = revertBalanceChange(login, originalHistoryItem.getType(), originalHistoryItem.getBalance());
        if (response.isSuccess()) {
            historyRepository.deleteById(historyItemId);
        }

        return response;
    }

    private SimpleResponse revertBalanceChange(String login, HistoryType type, Balance balance) {
        switch (type) {
            case income:
            case expense:
                balance.setValue(balance.getValue() * -1);
                break;
            case transfer:
                String account = balance.getAccountTo();
                String subAccount = balance.getSubAccountTo();
                balance.setAccountTo(balance.getAccount());
                balance.setSubAccountTo(balance.getSubAccount());
                balance.setAccount(account);
                balance.setSubAccount(subAccount);
                break;
            case exchange:
                Currency currency = balance.getNewCurrency();
                Double value = balance.getNewValue();
                balance.setNewCurrency(balance.getCurrency());
                balance.setNewValue(balance.getValue());
                balance.setCurrency(currency);
                balance.setValue(value);
                break;
        }

        return userAPI.updateUserBalance(login, type, balance);
    }

    private void addUnusedCurrencyConversions(HistoryItem historyItem) {
        Balance balance = historyItem.getBalance();
        Map<Currency, Double> alternativeCurrency = balance.getAlternativeCurrency();
        if (alternativeCurrency != null && alternativeCurrency.size() < Currency.values().length) {
            Set<Currency> missedCurrencies = Arrays.stream(Currency.values())
                    .filter(currency -> !currency.equals(balance.getCurrency()))
                    .filter(currency -> !alternativeCurrency.containsKey(currency))
                    .collect(Collectors.toSet());
            List<CurrencyDetail> currencyDetails = currencyRepository.getByYearAndMonthAndDayAndNameIn(historyItem.getYear(), historyItem.getMonth(), historyItem.getDay(), missedCurrencies);
            if (currencyDetails.isEmpty()) {
                LocalDate today = LocalDate.now();
                currencyDetails = currencyRepository.getByYearAndMonthAndDayAndNameIn(today.getYear(), today.getMonth().getValue(), today.getDayOfMonth(), missedCurrencies);
            }
            currencyDetails.forEach(currencyDetail -> {
                Double alternativeValue = balance.getValue() / currencyDetail.getConversions().get(balance.getCurrency());
                alternativeCurrency.put(currencyDetail.getName(), new BigDecimal(alternativeValue).setScale(2, RoundingMode.HALF_UP).doubleValue());
            });
        }
    }
}