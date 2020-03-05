package by.bk.entity.history;

import by.bk.controller.model.request.DateRequest;
import by.bk.controller.model.request.SmsRequest;
import by.bk.controller.model.response.DynamicReportResponse;
import by.bk.controller.model.response.SimpleResponse;
import by.bk.controller.model.response.SummaryReportResponse;
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
import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.*;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Sergey Koval
 */
@Service
public class HistoryService implements HistoryAPI {
    private static final Log LOG = LogFactory.getLog(HistoryService.class);
    private static final DecimalFormat CURRENCY_FORMAT = new DecimalFormat("##0.00", DecimalFormatSymbols.getInstance(Locale.US));
    private static final ZoneId MINSK_TIMEZONE = ZoneId.of("Europe/Minsk");

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
        Query query = Query.query(Criteria.where("user").is(login).and("notProcessed").ne(true))
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
    public SimpleResponse addHistoryItemsFromSms(String login, String deviceId, List<SmsRequest> smsItems) {
        Set<String> ids = smsItems.stream()
                .peek(smsRequest -> smsRequest.getSms().setDeviceId(deviceId))
                .map(smsRequest -> {
                    Sms sms = smsRequest.getSms();
                    Instant instant = Instant.ofEpochMilli(sms.getSmsTimestamp());
                    LocalDate smsDate = LocalDate.ofInstant(instant, MINSK_TIMEZONE);
                    return new HistoryItem(login, smsDate.getYear(), smsDate.getMonthValue(), smsDate.getDayOfMonth(), new Balance(smsRequest.getAccount(), smsRequest.getSubAccount()), sms);
                })
                .map(historyItem -> saveHistoryItem(login, historyItem).getId())
                .collect(Collectors.toSet());
        return smsItems.size() == ids.size() ? SimpleResponse.success() : SimpleResponse.fail();
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

    @Override
    public List<HistoryItem> getFiltered(String login, DateRequest startPeriod, DateRequest endPeriod, List<List<String>> operations, List<List<String>> accounts) {
        List<AggregationOperation> pipes = new ArrayList<>();
        Criteria periodCriteria = preparePeriodsCriteria(login, startPeriod, endPeriod);
        if (!operations.isEmpty()) {
            periodCriteria.and("type").in(operations.stream().map(operationsHierarchy -> operationsHierarchy.get(0)).collect(Collectors.toList()));
        }
        pipes.add(Aggregation.match(periodCriteria));

        if (!operations.isEmpty()) {
            pipes.add(Aggregation.match(prepareOperationsCriteria(operations)));
        }

        if (!accounts.isEmpty()) {
            pipes.add(Aggregation.match(prepareAccountsCriteria(accounts, true)));
        }

        pipes.add(Aggregation.sort(Sort.by(Sort.Order.desc("year"), Sort.Order.desc("month"), Sort.Order.desc("day"))));

        Aggregation aggregation = Aggregation.newAggregation(pipes);
        return mongoTemplate.aggregate(aggregation, "history", HistoryItem.class).getMappedResults();
    }

    @Override
    public Collection<SummaryReportResponse> getPeriodSummary(String login, DateRequest startPeriod, DateRequest endPeriod, List<List<String>> operations, List<List<String>> accounts, List<String> currencies) {
        List<AggregationOperation> pipes = new ArrayList<>();

        Criteria initialFilter = preparePeriodsCriteria(login, startPeriod, endPeriod);
        initialFilter.and("type").is(operations.get(0).get(0));
        if (!currencies.isEmpty()) {
            initialFilter.and("balance.currency").in(currencies);
        }
        pipes.add(Aggregation.match(initialFilter));

        if (!operations.isEmpty()) {
            pipes.add(Aggregation.match(prepareOperationsCriteria(operations)));
        }
        if (!accounts.isEmpty()) {
            pipes.add(Aggregation.match(prepareAccountsCriteria(accounts, false)));
        }

        pipes.add(Aggregation.group("category", "subCategory", "balance.currency").sum("balance.value").as("balanceValue"));

        Aggregation aggregation = Aggregation.newAggregation(pipes);
        List<SummaryReportItem> items = mongoTemplate.aggregate(aggregation, "history", SummaryReportItem.class).getMappedResults();

        //just one operation hierarchy provided
        if (operations.size() == 1) {
            return collectSummaryReport(operations.get(0).size() == 1, items.stream());
        } else {
            //separate operations where full category was provided and just some sub categories
            Map<Boolean, List<List<String>>> categorySubCategoryItems = operations.stream().collect(Collectors.partitioningBy(item -> item.size() < 3));
            return CollectionUtils.union(
                    //process operations with full categories
                    collectSummaryReport(
                            true,
                            categorySubCategoryItems.get(Boolean.TRUE).stream()
                                    .flatMap(hierarchy -> items.stream().filter(item -> StringUtils.equals(item.getCategory(), hierarchy.get(1))))
                    ),
                    //process operations with some specific sub categories
                    collectSummaryReport(
                            false,
                            categorySubCategoryItems.get(Boolean.FALSE).stream()
                                    .flatMap(hierarchy -> items.stream()
                                            .filter(item -> StringUtils.equals(item.getCategory(), hierarchy.get(1)))
                                            .filter(item -> StringUtils.equals(item.getSubCategory(), hierarchy.get(2))))
                    )
            );
        }
    }

    @Override
    public Collection<DynamicReportResponse> getPeriodDynamic(String login, DateRequest startPeriod, DateRequest endPeriod, List<List<String>> operations, Currency currency) {
        List<AggregationOperation> pipes = new ArrayList<>();
        int month = startPeriod.getMonth();
        int year = startPeriod.getYear();
        List<Criteria> orPeriods = new ArrayList<>();
        while (year < endPeriod.getYear() || month <= endPeriod.getMonth()) {
            orPeriods.add(Criteria.where("year").is(year).and("month").is(month));
            if (month == 12) {
                year++;
                month = 1;
            } else {
                month++;
            }
        }

        Criteria initialFilter = Criteria.where("user").is(login)
                .and("type").is(operations.get(0).get(0))
                .orOperator(orPeriods.toArray(new Criteria[0]));
        pipes.add(Aggregation.match(initialFilter));
        pipes.add(Aggregation.match(prepareOperationsCriteria(operations)));
        pipes.add(Aggregation.group("year", "month", "category", "subCategory", "balance.currency").sum("balance.value").as("balanceValue"));

        Aggregation aggregation = Aggregation.newAggregation(pipes);
        List<DynamicReportItem> items = mongoTemplate.aggregate(aggregation, "history", DynamicReportItem.class).getMappedResults();

        LocalDate today = LocalDate.now();
        Map<Currency, CurrencyDetail> currencies = currencyRepository.getByYearAndMonthAndDay(today.getYear(), today.getMonth().getValue(), today.getDayOfMonth()).stream()
                .collect(Collectors.toMap(CurrencyDetail::getName, currencyDetail -> currencyDetail));

        //just one operation hierarchy provided
        if (operations.size() == 1) {
            return collectDynamicReport(operations.get(0).size() == 1, currency, currencies, items.stream());
        } else {
            //separate operations where full category was provided and just some sub categories
            Map<Boolean, List<List<String>>> categorySubCategoryItems = operations.stream().collect(Collectors.partitioningBy(item -> item.size() < 3));
            return CollectionUtils.union(
                    //process operations with full categories
                    collectDynamicReport(
                            true,
                            currency,
                            currencies,
                            categorySubCategoryItems.get(Boolean.TRUE).stream()
                                    .flatMap(hierarchy -> items.stream().filter(item -> StringUtils.equals(item.getCategory(), hierarchy.get(1))))
                    ),
                    //process operations with some specific sub categories
                    collectDynamicReport(
                            false,
                            currency,
                            currencies,
                            categorySubCategoryItems.get(Boolean.FALSE).stream()
                                    .flatMap(hierarchy -> items.stream()
                                            .filter(item -> StringUtils.equals(item.getCategory(), hierarchy.get(1)))
                                            .filter(item -> StringUtils.equals(item.getSubCategory(), hierarchy.get(2))))
                    )
            );
        }
    }

    @Override
    public SimpleResponse getDeviceSms(String login, String deviceId, Integer smsIndex) {
        Query query = Query.query(Criteria.where("user").is(login).and("sms.deviceId").is(deviceId))
                .with(new Sort(Sort.Direction.DESC, "sms.smsTimestamp"))
                .skip(smsIndex)
                .limit(1);
        HistoryItem historyItem = mongoTemplate.findOne(query, HistoryItem.class);
        return historyItem != null ? SimpleResponse.successWithDetails(historyItem.getSms()) : SimpleResponse.fail("MISSED");
    }

    private Collection<SummaryReportResponse> collectSummaryReport(boolean ignoreSubCategories, Stream<SummaryReportItem> items) {
        return items.collect(Collectors.toMap(
                item -> ignoreSubCategories ? item.getCategory() : new MutablePair<>(item.getCategory(), item.getSubCategory()),
                item -> new SummaryReportResponse(item, ignoreSubCategories),
                (SummaryReportResponse oldItem, SummaryReportResponse newItem) -> {
                    Map<Currency, Double> newValues = newItem.getValues();
                    Currency newCurrency = newValues.keySet().iterator().next();
                    oldItem.getValues().compute(newCurrency, (currency, value) -> value == null ? newValues.get(newCurrency) : value + newValues.get(newCurrency));
                    return oldItem;
                })
        ).values();
    }

    private Collection<DynamicReportResponse> collectDynamicReport(boolean ignoreSubCategories, Currency currency, Map<Currency, CurrencyDetail> currencies, Stream<DynamicReportItem> items) {
        return items
                .peek(item -> {
                    if(!currency.equals(item.getCurrency())) {
                        Double currencyConversion = currencies.get(item.getCurrency()).getConversions().get(currency);
                        item.setBalanceValue(item.getBalanceValue() * currencyConversion);
                        item.setCurrency(currency);
                    }
                }).collect(
                        Collectors.groupingBy(
                            item -> new MutablePair<>(item.getYear(), item.getMonth()),
                            Collectors.toMap(
                                    item -> ignoreSubCategories ? item.getCategory() : new MutablePair<>(item.getCategory(), item.getSubCategory()),
                                    item -> new DynamicReportResponse(item, ignoreSubCategories),
                                    (DynamicReportResponse oldItem, DynamicReportResponse newItem) -> {
                                        oldItem.setValue(oldItem.getValue() + newItem.getValue());
                                        return oldItem;
                                    }
                            )
                        )
                ).values().stream()
                    .flatMap(dynamicReportResponseMap -> dynamicReportResponseMap.values().stream())
                    .peek(item -> item.setValue(Double.parseDouble(CURRENCY_FORMAT.format(item.getValue()))))
                    .collect(Collectors.toList());
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

    private Criteria preparePeriodsCriteria(String login, DateRequest startPeriod, DateRequest endPeriod) {
        Criteria criteria = Criteria.where("user").is(login);
        if (startPeriod.getYear().equals(endPeriod.getYear()) && startPeriod.getMonth().equals(endPeriod.getMonth())) {
            return criteria.and("year").is(startPeriod.getYear())
                    .and("month").is(startPeriod.getMonth())
                    .and("day").gte(startPeriod.getDay()).lte(endPeriod.getDay());
        }

        List<Criteria> orPeriods = new ArrayList<>();
        orPeriods.add(Criteria.where("year").is(startPeriod.getYear()).and("month").is(startPeriod.getMonth()).and("day").gte(startPeriod.getDay()));
        orPeriods.add(Criteria.where("year").is(endPeriod.getYear()).and("month").is(endPeriod.getMonth()).and("day").lte(endPeriod.getDay()));
        if (!nearPeriods(startPeriod, endPeriod)) {
            switchToNextPeriod(startPeriod);
            while (startPeriod.getYear() < endPeriod.getYear() || (startPeriod.getYear().equals(endPeriod.getYear()) && startPeriod.getMonth() < endPeriod.getMonth())) {
                orPeriods.add(Criteria.where("year").is(startPeriod.getYear()).and("month").is(startPeriod.getMonth()));
                switchToNextPeriod(startPeriod);
            }
        }
        return criteria.orOperator(orPeriods.toArray(new Criteria[0]));
    }

    private Criteria prepareOperationsCriteria(List<List<String>> operations) {
        List<Criteria> orCategories = new ArrayList<>();
        operations.forEach(operationsHierarchy -> {
            Iterator<String> iterator = operationsHierarchy.iterator();
            Criteria criteria = Criteria.where("type").is(iterator.next());
            if (iterator.hasNext()) {
                criteria.and("category").is(iterator.next());
                if (iterator.hasNext()) {
                    criteria.and("subCategory").is(iterator.next());
                }
            }
            orCategories.add(criteria);
        });

        return new Criteria().orOperator(orCategories.toArray(new Criteria[0]));
    }

    private Criteria prepareAccountsCriteria(List<List<String>> accounts, boolean includeToAlternative) {
        List<Criteria> orAccounts = new ArrayList<>();
        accounts.forEach(accountsHierarchy -> {
            Criteria criteriaTo = null;
            Iterator<String> iterator = accountsHierarchy.iterator();
            String account = iterator.next();
            Criteria criteria = Criteria.where("balance.account").is(account);
            if (includeToAlternative) {
                criteriaTo = Criteria.where("balance.accountTo").is(account);
            }
            if (iterator.hasNext()) {
                String subAccount = iterator.next();
                criteria.and("balance.subAccount").is(subAccount);
                if (includeToAlternative) {
                    criteriaTo.and("balance.subAccountTo").is(subAccount);
                }
            }
            orAccounts.add(criteria);
            if (includeToAlternative) {
                orAccounts.add(criteriaTo);
            }
        });

        return new Criteria().orOperator(orAccounts.toArray(new Criteria[0]));
    }

    private boolean nearPeriods(DateRequest startPeriod, DateRequest endPeriod) {
        return (startPeriod.getYear().equals(endPeriod.getYear()) && endPeriod.getMonth() - startPeriod.getMonth() == 1)
                || (endPeriod.getYear() - startPeriod.getYear() == 1 && startPeriod.getMonth() == 12 && endPeriod.getMonth() == 1);
    }

    private void switchToNextPeriod(DateRequest period) {
        if (period.getMonth() == 12) {
            period.setYear(period.getYear() + 1);
            period.setMonth(1);
        } else {
            period.setMonth(period.getMonth() + 1);
        }
    }
}
