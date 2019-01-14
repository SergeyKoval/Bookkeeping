package by.bk.entity.history;

import by.bk.controller.model.response.SimpleResponse;
import by.bk.entity.budget.BudgetAPI;
import by.bk.entity.budget.exception.HistoryItemMissedException;
import by.bk.entity.currency.Currency;
import by.bk.entity.currency.CurrencyDetail;
import by.bk.entity.currency.CurrencyRepository;
import by.bk.entity.user.UserAPI;
import by.bk.entity.user.UserRepository;
import by.bk.entity.user.model.SubCategoryType;
import by.bk.entity.user.model.UserCurrency;
import org.apache.commons.collections4.CollectionUtils;
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
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Supplier;
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
    @Autowired
    private BudgetAPI budgetAPI;
    @Autowired
    private UserRepository userRepository;

    @Override
    public List<HistoryItem> getPagePortion(String login, int page, int limit) {
        Query query = Query.query(Criteria.where("user").is(login))
                .with(Sort.by(Sort.Order.desc("year"), Sort.Order.desc("month"), Sort.Order.desc("day")))
                .skip((page -1) * limit)
                .limit(limit);

        List<Currency> usedCurrencies = userRepository.getUserCurrencies(login).getCurrencies().stream().map(UserCurrency::getName).collect(Collectors.toList());
        Collection<Currency> projectCurrencies = CollectionUtils.disjunction(Arrays.asList(Currency.values()), usedCurrencies);
        projectCurrencies.forEach(currency -> query.fields().exclude(StringUtils.join("balance.alternativeCurrency.", currency.name())));

        return mongoTemplate.find(query, HistoryItem.class);
    }

    @Override
    public List<HistoryItem> getSuitable(String login, String category, String subCategory, SubCategoryType subCategoryType) {
        return historyRepository.getAllByUserAndCategoryAndSubCategoryAndType(login, category, subCategory, subCategoryType.convertToHistoryType());
    }

    @Override
    public SimpleResponse addHistoryItem(String login, HistoryItem historyItem, boolean changeGoalStatus) {
        addUnusedCurrencyConversions(historyItem);
        HistoryItem savedHistoryItem = saveHistoryItem(login, historyItem);

        if (StringUtils.isBlank(savedHistoryItem.getId())) {
            LOG.error(StringUtils.join("Error adding history item ", savedHistoryItem, " for user ", login));
            return SimpleResponse.fail("ERROR");
        }

        SimpleResponse response = userAPI.updateUserBalance(login, savedHistoryItem.getType(), savedHistoryItem.cloneBalance());
        if (!response.isSuccess()) {
            LOG.error(StringUtils.join("Error updating user balance based on the history item ", savedHistoryItem, " for user ", login));
            historyRepository.delete(savedHistoryItem);
            return response;
        }

        return affectBudget(historyItem.getType()) ? budgetAPI.addHistoryItem(login, savedHistoryItem, changeGoalStatus) : response;
    }

    @Override
    public HistoryItem addBalanceHistoryItem(String login, Currency currency, String accountTitle, String subAccountTitle, Supplier<Double> value) {
        LocalDateTime now = LocalDateTime.now();
        Balance historyBalance = new Balance();
        historyBalance.setAccount(accountTitle);
        historyBalance.setSubAccount(subAccountTitle);
        historyBalance.setValue(value.get());
        historyBalance.setCurrency(currency);

        HistoryItem historyItem = new HistoryItem();
        historyItem.setType(HistoryType.balance);
        historyItem.setYear(now.getYear());
        historyItem.setMonth(now.getMonthValue());
        historyItem.setDay(now.getDayOfMonth());
        historyItem.setBalance(historyBalance);
        return saveHistoryItem(login, historyItem);
    }

    @Override
    public SimpleResponse editHistoryItem(String login, HistoryItem historyItem, boolean changeGoalStatus, boolean changeOriginalGoalStatus) {
        HistoryItem originalHistoryItem = getById(login, historyItem.getId());
        SimpleResponse response = revertBalanceChange(login, originalHistoryItem.getType(), originalHistoryItem.cloneBalance());
        if (response.isSuccess()) {
            historyItem = saveHistoryItem(login, historyItem);
            response = userAPI.updateUserBalance(login, historyItem.getType(), historyItem.cloneBalance());

            if (!response.isSuccess()) {
                LOG.error(StringUtils.join("Error on second updating balance for changing history item. Step 1 success update, which need to be reverted: ", originalHistoryItem));
                historyRepository.save(originalHistoryItem);
                userAPI.updateUserBalance(login, historyItem.getType(), originalHistoryItem.getBalance());
            }
        }

        return affectBudget(originalHistoryItem.getType()) ? budgetAPI.editHistoryItem(login, originalHistoryItem, changeOriginalGoalStatus, historyItem, changeGoalStatus) : response;
    }

    @Override
    public SimpleResponse deleteHistoryItem(String login, String historyItemId, boolean changeGoalStatus) {
        HistoryItem originalHistoryItem = getById(login, historyItemId);
        SimpleResponse response = revertBalanceChange(login, originalHistoryItem.getType(), originalHistoryItem.cloneBalance());
        if (response.isSuccess()) {
            historyRepository.deleteById(historyItemId);
        }

        return affectBudget(originalHistoryItem.getType()) ? budgetAPI.deleteHistoryItem(login, originalHistoryItem, changeGoalStatus) : response;
    }

    @Override
    public HistoryItem getById(String login, String historyItemId) {
        return historyRepository.findById(historyItemId).orElseThrow(() -> new HistoryItemMissedException(login, historyItemId));
    }

    private boolean affectBudget(HistoryType type) {
        return HistoryType.income.equals(type) || HistoryType.expense.equals(type);
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

    private HistoryItem saveHistoryItem(String login, HistoryItem historyItem) {
        int order = 1 + historyRepository.getAllDayHistoryItemsWithOrder(login, historyItem.getYear(), historyItem.getMonth(), historyItem.getDay())
                .stream()
                .max(Comparator.comparingInt(HistoryItem::getOrder))
                .map(HistoryItem::getOrder)
                .orElse(0);

        historyItem.setOrder(order);
        historyItem.setUser(login);
        return historyRepository.save(historyItem);
    }
}