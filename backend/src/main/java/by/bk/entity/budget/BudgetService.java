package by.bk.entity.budget;

import by.bk.controller.exception.ItemAlreadyExistsException;
import by.bk.controller.model.request.BudgetCategoryStatisticsRequest;
import by.bk.controller.model.request.BudgetCloseMonthRequest;
import by.bk.controller.model.response.SimpleResponse;
import by.bk.entity.budget.exception.*;
import by.bk.entity.budget.model.*;
import by.bk.entity.currency.Currency;
import by.bk.entity.history.Balance;
import by.bk.entity.history.HistoryItem;
import by.bk.entity.history.HistoryType;
import by.bk.entity.user.model.SubCategoryType;
import com.mongodb.client.result.UpdateResult;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.*;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Sergey Koval
 */
@Service
public class BudgetService implements BudgetAPI {
    private static final Log LOG = LogFactory.getLog(BudgetService.class);

    @Autowired
    private BudgetRepository budgetRepository;
    @Autowired
    private MongoTemplate mongoTemplate;

    @Override
    public Budget getMonthBudget(String login, int year, int month) {
        Optional<Budget> budget = budgetRepository.findByUserAndYearAndMonth(login, year, month);
        return budget.orElseGet(() -> initEmptyMonthBudget(login, year, month));
    }

    @Override
    public SimpleResponse changeGoalDoneStatus(String login, String budgetId, HistoryType type, String categoryTitle, String goalTitle, boolean doneStatus) {
        Budget budget = validateAndGetBudget(login, budgetId);
        BudgetDetails budgetDetails = chooseBudgetDetails(budget, type);
        BudgetCategory category = chooseBudgetCategory(budgetDetails, categoryTitle, login, budgetId);
        BudgetGoal goal = chooseBudgetGoal(category, goalTitle, login, budgetId);
        goal.setDone(doneStatus);

        Query query = Query.query(Criteria.where("id").is(budgetId));
        Update update = Update.update(StringUtils.join(type.name(), ".categories.", budgetDetails.getCategories().indexOf(category), ".goals.", category.getGoals().indexOf(goal), ".done"), doneStatus);
        UpdateResult updateResult = mongoTemplate.updateFirst(query, update, Budget.class);

        return updateResult.getModifiedCount() == 1 ? SimpleResponse.success() : SimpleResponse.fail();
    }

    @Override
    public SimpleResponse toggleBudgetDetails(String login, String budgetId, HistoryType type, boolean opened) {
        validateAndGetBudget(login, budgetId);
        Query query = Query.query(Criteria.where("id").is(budgetId));
        Update update = Update.update(StringUtils.join(type.name(), ".opened"), opened);
        UpdateResult updateResult = mongoTemplate.updateFirst(query, update, Budget.class);

        return updateResult.getModifiedCount() == 1 ? SimpleResponse.success() : SimpleResponse.fail();
    }

    @Override
    public SimpleResponse addBudgetCategory(String login, String budgetId, HistoryType type, String categoryTitle, List<CurrencyBalanceValue> currencyBalances) {
        BudgetDetails budgetDetails = chooseBudgetDetails(validateAndGetBudget(login, budgetId), type);
        if (budgetDetails.getCategories().stream().anyMatch(budgetCategory -> StringUtils.equals(categoryTitle, budgetCategory.getTitle()))) {
            return SimpleResponse.alreadyExistsFail();
        }

        BudgetCategory category = new BudgetCategory();
        category.setTitle(categoryTitle);
        currencyBalances.forEach(currencyBalance -> category.getBalance().put(currencyBalance.getCurrency(), new BalanceValue(0d, currencyBalance.getCompleteValue())));

        Query query = Query.query(Criteria.where("id").is(budgetId));
        Update update = new Update().addToSet(type.name() + ".categories").value(category);
        currencyBalances.forEach(currencyBalance -> {
            String preffix = StringUtils.join( type.name(), ".balance.", currencyBalance.getCurrency());
            update.inc(preffix + ".value", 0d);
            update.inc(preffix + ".completeValue", currencyBalance.getCompleteValue());
        });
        UpdateResult updateResult = mongoTemplate.updateFirst(query, update, Budget.class);

        return updateResult.getModifiedCount() == 1 ? SimpleResponse.success() : SimpleResponse.fail();
    }

    @Override
    public SimpleResponse editBudgetCategory(String login, String budgetId, HistoryType type, String categoryTitle, List<CurrencyBalanceValue> currencyBalances) {
        BudgetDetails budgetDetails = chooseBudgetDetails(validateAndGetBudget(login, budgetId), type);
        BudgetCategory category = chooseBudgetCategory(budgetDetails, categoryTitle, login, budgetId);

        Map<Currency, CurrencyBalanceValue> updateBalances = currencyBalances.stream().collect(Collectors.toMap(CurrencyBalanceValue::getCurrency, currencyBalanceValue -> currencyBalanceValue));
        category.getBalance().forEach((currency, balanceValue) -> {
            Double usedValue = balanceValue.getValue();
            if (usedValue > 0 && (!updateBalances.containsKey(currency) || updateBalances.get(currency).getCompleteValue() < usedValue) && BooleanUtils.isNotTrue(updateBalances.get(currency).getConfirmValue())) {
                LOG.error("Trying to update category with complete currency value less then already used.");
                throw new InvalidBudgetPlanningException(login, budgetId, categoryTitle, currency, usedValue, updateBalances.get(currency).getCompleteValue());
            }
        });

        Map<Currency, Double> budgetPlan = new HashMap<>();
        Map<Currency, BalanceValue> categoryPlan = new HashMap<>();
        category.getBalance().forEach((currency, balanceValue) -> budgetPlan.put(currency, balanceValue.getCompleteValue() * -1));

        currencyBalances.forEach(currencyBalance -> {
            Currency currency = currencyBalance.getCurrency();
            Double newCompleteValue = currencyBalance.getCompleteValue();

            budgetPlan.compute(currency, (budgetPlanCurrency, value) -> value == null ? newCompleteValue : value + newCompleteValue);
            BalanceValue balanceValue = category.getBalance().get(currency);
            categoryPlan.put(currency, new BalanceValue(balanceValue != null ? balanceValue.getValue() : 0, newCompleteValue));
        });

        Query query = Query.query(Criteria.where("id").is(budgetId));
        Update update = Update.update(StringUtils.join(type.name(), ".categories.", budgetDetails.getCategories().indexOf(category), ".balance"), categoryPlan);
        budgetPlan.forEach((currency, value) -> {
            String preffix = StringUtils.join( type.name(), ".balance.", currency);
            update.inc(preffix + ".value", 0d);
            update.inc(preffix + ".completeValue", value);
        });
        UpdateResult updateResult = mongoTemplate.updateFirst(query, update, Budget.class);

        return updateResult.getMatchedCount() == 1 ? SimpleResponse.success() : SimpleResponse.fail();
    }

    @Override
    public SimpleResponse addBudgetGoal(String login, String budgetId, Integer year, Integer month, HistoryType type, String categoryTitle, String goalTitle, CurrencyBalanceValue balance) {
        Budget budget = StringUtils.isNotBlank(budgetId) ? validateAndGetBudget(login, budgetId) : getMonthBudget(login, year, month);
        BudgetDetails budgetDetails = chooseBudgetDetails(budget, type);
        Optional<BudgetCategory> optionalCategory = budgetDetails.getCategories().stream()
                .filter(budgetCategory -> StringUtils.equals(budgetCategory.getTitle(), categoryTitle))
                .findFirst();

        balance.setValue(0d);
        Currency currency = balance.getCurrency();
        Double balanceCompleteValue = balance.getCompleteValue();

        BudgetGoal goal = new BudgetGoal();
        goal.setTitle(goalTitle);
        goal.setBalance(balance);

        Update update = new Update();
        if (!optionalCategory.isPresent()) {
            BudgetCategory category = new BudgetCategory();
            category.setTitle(categoryTitle);
            category.getBalance().put(currency, new BalanceValue(0d, balanceCompleteValue));
            category.getGoals().add(goal);

            update.addToSet(type.name() + ".categories").value(category);
        } else {
            BudgetCategory category = optionalCategory.get();
            if (category.getGoals().stream().anyMatch(budgetGoal -> StringUtils.equals(goalTitle, budgetGoal.getTitle()))) {
                return SimpleResponse.alreadyExistsFail();
            }

            String categoryQuery = StringUtils.join(type.name(), ".categories.", budgetDetails.getCategories().indexOf(category));
            String categoryBalanceQuery = StringUtils.join(categoryQuery, ".balance.", currency.name());
            update.addToSet(categoryQuery + ".goals").value(goal)
                    .inc(categoryBalanceQuery + ".value", 0d)
                    .inc(categoryBalanceQuery + ".completeValue", balanceCompleteValue);
        }

        String balanceQuery = StringUtils.join(type.name(), ".balance.", currency.name());
        Query query = Query.query(Criteria.where("id").is(budget.getId()));
        update.inc(balanceQuery + ".value", 0d).inc(balanceQuery + ".completeValue", balanceCompleteValue);
        UpdateResult updateResult = mongoTemplate.updateFirst(query, update, Budget.class);
        return updateResult.getModifiedCount() == 1 ? SimpleResponse.success() : SimpleResponse.fail();
    }

    @Override
    public SimpleResponse editBudgetGoal(String login, String budgetId, Integer year, Integer month, HistoryType type, String categoryTitle, String originalGoalTitle, String goalTitle, CurrencyBalanceValue balance, boolean changeGoalStatus) {
        Budget budget = validateAndGetBudget(login, budgetId);
        BudgetDetails budgetDetails = chooseBudgetDetails(budget, type);
        BudgetCategory category = chooseBudgetCategory(budgetDetails, categoryTitle, login, budgetId);
        BudgetGoal originalGoal = chooseBudgetGoal(category, originalGoalTitle, login, budgetId);

        String categoryQuery = StringUtils.join(type.name(), ".categories.", budgetDetails.getCategories().indexOf(category));

        Update update = new Update();
//      If month and year was not changed
        if (budget.getYear().equals(year) && budget.getMonth().equals(month)) {
            editBudgetGoalSameMonth(update, budgetDetails, originalGoalTitle, goalTitle, category, balance, originalGoal, type, categoryQuery, changeGoalStatus);
        } else {
            CurrencyBalanceValue originalGoalBalance = originalGoal.getBalance();
            SimpleResponse simpleResponse = addBudgetGoal(login, null, year, month, type, categoryTitle, goalTitle, balance);
            if (!simpleResponse.isSuccess()) {
                LOG.error("Fail status on adding goal to another budget");
                return simpleResponse;
            }
//          If whole category will not be removed - remove goal
            if (!(category.getBalance().get(originalGoalBalance.getCurrency()).getCompleteValue().equals(originalGoalBalance.getCompleteValue()) && category.getBalance().size() == 1)) {
                update.pull(categoryQuery + ".goals", Collections.singletonMap("title", originalGoal.getTitle()));
            }
            changeOriginalBudgetAndCategoryBalances(update, budgetDetails, category, originalGoalBalance, categoryQuery, type);
        }

        Query query = Query.query(Criteria.where("id").is(budgetId));
        UpdateResult updateResult = mongoTemplate.updateFirst(query, update, Budget.class);
        return updateResult.getMatchedCount() == 1 ? SimpleResponse.success() : SimpleResponse.fail();
    }

    @Override
    public SimpleResponse updateBudgetLimit(String login, String budgetId, HistoryType type, List<CurrencyBalanceValue> currencyBalances) {
        Budget budget = validateAndGetBudget(login, budgetId);
        BudgetDetails budgetDetails = chooseBudgetDetails(budget, type);

        Map<Currency, CurrencyBalanceValue> balances = currencyBalances.stream().collect(Collectors.toMap(CurrencyBalanceValue::getCurrency, currencyBalanceValue -> currencyBalanceValue));
        Map<Currency, Double> minBalance = budgetDetails.getCategories().stream()
                .map(BudgetCategory::getBalance)
                .flatMap(balance -> balance.keySet().stream().map(currency -> new CurrencyBalanceValue(currency, balance.get(currency))))
                .collect(Collectors.toMap(CurrencyBalanceValue::getCurrency, CurrencyBalanceValue::getCompleteValue, (oldValue, newValue) -> oldValue + newValue));
        if (minBalance.keySet().stream().anyMatch(currency -> !balances.containsKey(currency) || balances.get(currency).getCompleteValue() < minBalance.get(currency))) {
            LOG.warn("Some balance value is less then minimum sum of categories values.");
            return SimpleResponse.fail();
        }

        Query query = Query.query(Criteria.where("id").is(budgetId));
        Update update = new Update();
        currencyBalances.forEach(balance -> update.set(type.name() + ".balance." + balance.getCurrency() + ".completeValue", balance.getCompleteValue()));
        UpdateResult updateResult = mongoTemplate.updateFirst(query, update, Budget.class);
        return updateResult.getMatchedCount() == 1 ? SimpleResponse.success() : SimpleResponse.fail();
    }

    @Override
    public SimpleResponse removeGoal(String login, String budgetId, HistoryType type, String categoryTitle, String goalTitle) {
        Budget budget = validateAndGetBudget(login, budgetId);
        BudgetDetails budgetDetails = chooseBudgetDetails(budget, type);
        BudgetCategory category = chooseBudgetCategory(budgetDetails, categoryTitle, login, budgetId);
        BudgetGoal goal = chooseBudgetGoal(category, goalTitle, login, budgetId);
        CurrencyBalanceValue goalBalance = goal.getBalance();

        String categoryQuery = StringUtils.join(type.name(), ".categories.", budgetDetails.getCategories().indexOf(category));
        Update update = new Update();
        if (!(category.getBalance().get(goalBalance.getCurrency()).getCompleteValue().equals(goalBalance.getCompleteValue()) && category.getBalance().size() == 1)) {
            update.pull(categoryQuery + ".goals", Collections.singletonMap("title", goal.getTitle()));
        }
        changeOriginalBudgetAndCategoryBalances(update, budgetDetails, category, goalBalance, categoryQuery, type);

        Query query = Query.query(Criteria.where("id").is(budgetId));
        UpdateResult updateResult = mongoTemplate.updateFirst(query, update, Budget.class);
        return updateResult.getModifiedCount() == 1 ? SimpleResponse.success() : SimpleResponse.fail();
    }

    @Override
    public SimpleResponse moveCategory(String login, String oldCategoryTitle, String newCategoryTitle, String subCategoryTitle, SubCategoryType subCategoryType, List<HistoryItem> historyItems) {
        historyItems.forEach(historyItem -> {
            deleteHistoryItem(login, historyItem, false);
            historyItem.setCategory(newCategoryTitle);
            addHistoryItem(login, historyItem, false);
            mongoTemplate.save(historyItem);
        });

        return SimpleResponse.success();
    }

    @Override
    public SimpleResponse removeCategory(String login, String budgetId, HistoryType type, String categoryTitle) {
        Budget budget = validateAndGetBudget(login, budgetId);
        BudgetDetails budgetDetails = chooseBudgetDetails(budget, type);
        BudgetCategory category = chooseBudgetCategory(budgetDetails, categoryTitle, login, budgetId);

        Update update = new Update();
        update.pull(type.name() + ".categories", Collections.singletonMap("title", categoryTitle));
        category.getBalance().forEach((currency, balanceValue) -> {
            String budgetBalanceCurrencyQuery = StringUtils.join(type.name(), ".balance." , currency);
            Double completeValue = balanceValue.getCompleteValue();
            if (budgetDetails.getBalance().get(currency).getCompleteValue().equals(completeValue)) {
                update.unset(budgetBalanceCurrencyQuery);
            } else {
                update.inc(budgetBalanceCurrencyQuery + ".completeValue", completeValue * -1);
            }
        });

        Query query = Query.query(Criteria.where("id").is(budgetId));
        UpdateResult updateResult = mongoTemplate.updateFirst(query, update, Budget.class);
        return updateResult.getModifiedCount() == 1 ? SimpleResponse.success() : SimpleResponse.fail();
    }

    @Override
    public SimpleResponse moveGoal(String login, String budgetId, Integer year, Integer month, HistoryType type, String categoryTitle, String goalTitle, Double movedValue) {
        Budget budget = validateAndGetBudget(login, budgetId);
        BudgetDetails budgetDetails = chooseBudgetDetails(budget, type);
        BudgetCategory category = chooseBudgetCategory(budgetDetails, categoryTitle, login, budgetId);
        BudgetGoal goal = chooseBudgetGoal(category, goalTitle, login, budgetId);
        CurrencyBalanceValue balance = goal.getBalance();

        CurrencyBalanceValue balanceValue = new CurrencyBalanceValue(0d, movedValue, balance.getCurrency());
        SimpleResponse simpleResponse = addBudgetGoal(login, null, year, month, type, categoryTitle, goalTitle, balanceValue);
        if (simpleResponse.isSuccess()) {
            Update update = new Update();
            String categoryQuery = StringUtils.join(type.name(), ".categories.", budgetDetails.getCategories().indexOf(category));
            editBudgetGoalSameMonth(update, budgetDetails, goalTitle, goalTitle, category, balance, goal, type, categoryQuery, true);

            Query query = Query.query(Criteria.where("id").is(budgetId));
            UpdateResult updateResult = mongoTemplate.updateFirst(query, update, Budget.class);
            return updateResult.getMatchedCount() == 1 ? SimpleResponse.success() : SimpleResponse.fail();
        } else {
            return simpleResponse;
        }
    }

    @Override
    public SimpleResponse addHistoryItem(String login, HistoryItem historyItem, boolean changeGoalStatus) {
        String goalTitle = historyItem.getGoal();
        Budget budget = getMonthBudget(login, historyItem.getYear(), historyItem.getMonth());
        String budgetId = budget.getId();
        BudgetDetails budgetDetails = chooseBudgetDetails(budget, historyItem.getType());
        Optional<BudgetCategory> optionalCategory = budgetDetails.getCategories().stream()
                .filter(budgetCategory -> StringUtils.equals(budgetCategory.getTitle(), historyItem.getCategory()))
                .findFirst();

        if (!optionalCategory.isPresent() && StringUtils.isNotBlank(goalTitle)) {
            LOG.error(StringUtils.join("History item is included in goal, but budget category is missed. Budget=", budgetId, ", type=", historyItem.getType(), ", category=", historyItem.getCategory(), ", goal=", goalTitle));
            return SimpleResponse.fail();
        }

        Balance historyBalance = historyItem.getBalance();
        Currency historyCurrency = historyBalance.getCurrency();
        Double historyValue = historyBalance.getValue();

        Update update = new Update();
        if (!optionalCategory.isPresent()) {
            BudgetCategory category = new BudgetCategory();
            category.setTitle(historyItem.getCategory());
            category.getBalance().put(historyCurrency, BalanceValue.initValue(historyValue));
            update.addToSet(historyItem.getType() + ".categories", category);
        } else {
            BudgetCategory category = optionalCategory.get();
            String categoriesQuery = StringUtils.join(historyItem.getType(), ".categories.", budgetDetails.getCategories().indexOf(category));
            update.inc(categoriesQuery + ".balance." + historyCurrency + ".value", historyValue);
            update.inc(categoriesQuery + ".balance." + historyCurrency + ".completeValue", 0d);

            if (StringUtils.isNotBlank(goalTitle)) {
                BudgetGoal goal = chooseBudgetGoal(category, goalTitle, login, budgetId);
                Currency goalCurrency = goal.getBalance().getCurrency();
                Double goalValue = goalCurrency.equals(historyCurrency) ? historyValue : historyBalance.getAlternativeCurrency().get(goalCurrency);
                String goalQuery = StringUtils.join(categoriesQuery, ".goals.", category.getGoals().indexOf(goal));
                update.inc(goalQuery + ".balance.value", goalValue);
                if (changeGoalStatus) {
                    update.set(goalQuery + ".done", !goal.isDone());
                }
            }
        }

        update.inc(historyItem.getType() + ".balance." + historyCurrency + ".value", historyValue);
        update.inc(historyItem.getType() + ".balance." + historyCurrency + ".completeValue", 0d);

        Query query = Query.query(Criteria.where("id").is(budgetId));
        UpdateResult updateResult = mongoTemplate.updateFirst(query, update, Budget.class);
        return updateResult.getModifiedCount() == 1 ? SimpleResponse.success() : SimpleResponse.fail();
    }

    @Override
    public SimpleResponse deleteHistoryItem(String login, HistoryItem historyItem, boolean changeGoalStatus) {
        Budget budget = getMonthBudget(login, historyItem.getYear(), historyItem.getMonth());
        String budgetId = budget.getId();
        BudgetDetails budgetDetails = chooseBudgetDetails(budget, historyItem.getType());
        BudgetCategory category = chooseBudgetCategory(budgetDetails, historyItem.getCategory(), login, budgetId);

        Balance historyBalance = historyItem.getBalance();
        Currency historyCurrency = historyBalance.getCurrency();
        Double historyValue = historyBalance.getValue() * -1;

        Update update = new Update();
        String categoriesQuery = StringUtils.join(historyItem.getType(), ".categories.", budgetDetails.getCategories().indexOf(category));
        update.inc(categoriesQuery + ".balance." + historyCurrency + ".value", historyValue);
        update.inc(categoriesQuery + ".balance." + historyCurrency + ".completeValue", 0d);

        if (StringUtils.isNotBlank(historyItem.getGoal())) {
            BudgetGoal goal = chooseBudgetGoal(category, historyItem.getGoal(), login, budgetId);
            Currency goalCurrency = goal.getBalance().getCurrency();
            Double goalValue = goalCurrency.equals(historyCurrency) ? historyValue : (historyBalance.getAlternativeCurrency().get(goalCurrency) * -1);
            String goalQuery = StringUtils.join(categoriesQuery, ".goals.", category.getGoals().indexOf(goal));
            update.inc(goalQuery + ".balance.value", goalValue);
            if (changeGoalStatus) {
                update.set(goalQuery + ".done", !goal.isDone());
            }
        }

        update.inc(historyItem.getType() + ".balance." + historyCurrency + ".value", historyValue);
        update.inc(historyItem.getType() + ".balance." + historyCurrency + ".completeValue", 0d);

        Query query = Query.query(Criteria.where("id").is(budgetId));
        UpdateResult updateResult = mongoTemplate.updateFirst(query, update, Budget.class);
        return updateResult.getModifiedCount() == 1 ? SimpleResponse.success() : SimpleResponse.fail();
    }

    @Override
    public SimpleResponse reviewBeforeRemoveHistoryItem(String login, HistoryItem historyItem) {
        Budget budget = getMonthBudget(login, historyItem.getYear(), historyItem.getMonth());
        BudgetDetails budgetDetails = chooseBudgetDetails(budget, historyItem.getType());
        BudgetCategory category = chooseBudgetCategory(budgetDetails, historyItem.getCategory(), login, budget.getId());
        BudgetGoal goal = chooseBudgetGoal(category, historyItem.getGoal(), login, budget.getId());
        CurrencyBalanceValue goalBalance = goal.getBalance();
        Currency goalCurrency = goalBalance.getCurrency();
        Balance historyBalance = historyItem.getBalance();

        Double minusValue = goalCurrency.equals(historyBalance.getCurrency()) ? historyBalance.getValue() : historyBalance.getAlternativeCurrency().get(goalCurrency);
        if (goal.isDone() && goalBalance.getValue() - minusValue < goalBalance.getCompleteValue()) {
            Map<String, Object> result = new HashMap<>();
            result.put("value", goalBalance.getValue() - minusValue);
            result.put("completeValue", goalBalance.getCompleteValue());
            result.put("currency", goalCurrency);
            return SimpleResponse.failWithDetails(result);
        } else {
            return SimpleResponse.success();
        }
    }

    @Override
    public SimpleResponse editHistoryItem(String login, HistoryItem originalHistoryItem, boolean changeOriginalGoalStatus, HistoryItem historyItem, boolean changeGoalStatus) {
        SimpleResponse revertResponse = deleteHistoryItem(login, originalHistoryItem, changeOriginalGoalStatus);
        if (revertResponse.isSuccess()) {
            SimpleResponse addResponse = addHistoryItem(login, historyItem, changeGoalStatus);
            if (!addResponse.isSuccess()) {
                LOG.error(StringUtils.join("Error adding history item on edit operation. login=", login
                        , ", originalHistoryItem=", originalHistoryItem, ", changeOriginalGoalStatus=", changeGoalStatus, ", historyItem=", historyItem, ", changeGoalStatus=", changeGoalStatus));
            }

            return addResponse;
        } else {
            LOG.error(StringUtils.join("Error reverting history item on edit operation. login=", login
                    , ", originalHistoryItem=", originalHistoryItem, ", changeOriginalGoalStatus=", changeGoalStatus, ", historyItem=", historyItem, ", changeGoalStatus=", changeGoalStatus));
            return revertResponse;
        }
    }

    @Override
    public SimpleResponse renameCategory(String login, String oldCategoryTitle, String newCategoryTitle) {
        Query budgetQuery = Query.query(Criteria.where("user").is(login)
                .orOperator(Criteria.where("expense.categories.title").is(oldCategoryTitle), Criteria.where("income.categories.title").is(oldCategoryTitle)));
        mongoTemplate.find(budgetQuery, Budget.class).stream()
                .peek(budget -> {
                    renameBudgetDetailsCategory(budget.getExpense(), oldCategoryTitle, newCategoryTitle);
                    renameBudgetDetailsCategory(budget.getIncome(), oldCategoryTitle, newCategoryTitle);
                }).forEach(budget -> budgetRepository.save(budget));
        return SimpleResponse.success();
    }

    @Override
    public List<BudgetStatistics> categoryStatistics(String login, BudgetCategoryStatisticsRequest request) {
        Criteria matchCriteria = Criteria.where("user").is(login)
                .and(request.getBudgetType() + ".categories.title").is(request.getCategory())
                .orOperator(Criteria.where("year").lt(request.getYear()), Criteria.where("year").is(request.getYear()).and("month").lt(request.getMonth()));
        MatchOperation matchStage = Aggregation.match(matchCriteria);
        SortOperation sortStage = Aggregation.sort(Sort.by(Sort.Order.desc("year"), Sort.Order.desc("month")));
        LimitOperation limitStage = Aggregation.limit(6);
        ProjectionOperation projectionOperation = Aggregation.project("year", "month", request.getBudgetType() + ".categories");

        Aggregation aggregation = Aggregation.newAggregation(matchStage, sortStage, limitStage, projectionOperation);
        AggregationResults<BudgetStatistics> result = mongoTemplate.aggregate(aggregation, "budget", BudgetStatistics.class);
        return result.getMappedResults().stream()
                .peek(budgetStatistics ->  {
                    budgetStatistics.setCategory(budgetStatistics.getCategories().stream()
                            .filter(budgetCategory -> StringUtils.equals(budgetCategory.getTitle(), request.getCategory()))
                            .findFirst()
                            .orElse(null));
                    budgetStatistics.setCategories(null);
                })
                .collect(Collectors.toList());
    }

    @Override
    public SimpleResponse closeMonth(String login, BudgetCloseMonthRequest request) {
        Budget budget = getMonthBudget(login, request.getYear(), request.getMonth());
        request.getPlaningCategories().forEach(categoryRequest -> {
            BudgetDetails budgetDetails = chooseBudgetDetails(budget, categoryRequest.getType());
            String categoryTitle = categoryRequest.getCategory().getTitle();
            BudgetCategory budgetCategory = chooseBudgetCategory(budgetDetails, categoryTitle).orElseGet(() -> {
                BudgetCategory newBudgetCategory = new BudgetCategory();
                newBudgetCategory.setTitle(categoryTitle);
                newBudgetCategory.setBalance(categoryRequest.getActionPlan());
                newBudgetCategory.getBalance().forEach((currency, balanceValue) -> balanceValue.setValue(0d));
                budgetDetails.getCategories().add(newBudgetCategory);
                return newBudgetCategory;
            });
            Map<Currency, BalanceValue> categoryBalance = budgetCategory.getBalance();
            categoryRequest.getActionPlan().forEach((currency, balanceValue) -> {
                if (categoryBalance.containsKey(currency)) {
                    categoryBalance.get(currency).setCompleteValue(balanceValue.getCompleteValue());
                } else {
                    categoryBalance.put(currency, balanceValue);
                }
            });

            categoryRequest.getGoalWrappers().forEach(goalRequest -> {
                String goalTitle = goalRequest.getGoal().getTitle();
                CurrencyBalanceValue goalBalance = goalRequest.getActionPlan().getBalance();
                BudgetGoal budgetGoal = chooseBudgetGoal(budgetCategory, goalTitle).orElseGet(() -> {
                    goalBalance.setValue(0d);
                    BudgetGoal newBudgetGoal = new BudgetGoal(false, goalTitle, goalBalance);
                    budgetCategory.getGoals().add(newBudgetGoal);
                    return newBudgetGoal;
                });
                CurrencyBalanceValue balance = budgetGoal.getBalance();
                balance.setCompleteValue(goalBalance.getCompleteValue());
            });
        });

        updateBudgetDetailsBalanceBasedOnCategories(budget.getIncome());
        updateBudgetDetailsBalanceBasedOnCategories(budget.getExpense());
        budgetRepository.save(budget);

        List<String> errors = closeMonthAnotherPeriodGoals(login, request);
        return errors.isEmpty() ? SimpleResponse.success() : SimpleResponse.failWithDetails(Collections.singletonMap("errors", errors));
    }

    private List<String> closeMonthAnotherPeriodGoals(String login, BudgetCloseMonthRequest request) {
        List<String> errors = new ArrayList<>();
        Map<ImmutablePair<Integer, Integer>, Budget> anotherBudgets = request.getAnotherMontGoals().stream()
                .map(goalRequest -> new ImmutablePair<>(goalRequest.getActionPlan().getYear(), goalRequest.getActionPlan().getMonth()))
                .distinct()
                .collect(Collectors.toMap(period -> period, period -> getMonthBudget(login, period.getLeft(), period.getRight())));

        request.getAnotherMontGoals().forEach(goalRequest -> {
            CurrencyBalanceValue goalBalance = goalRequest.getActionPlan().getBalance();
            Budget anotherBudget = anotherBudgets.get(new ImmutablePair<>(goalRequest.getActionPlan().getYear(), goalRequest.getActionPlan().getMonth()));
            BudgetDetails budgetDetails = chooseBudgetDetails(anotherBudget, goalRequest.getType());

            BudgetCategory budgetCategory = chooseBudgetCategory(budgetDetails, goalRequest.getCategory()).orElseGet(() -> {
                BudgetCategory newBudgetCategory = new BudgetCategory();
                newBudgetCategory.setTitle(goalRequest.getCategory());
                budgetDetails.getCategories().add(newBudgetCategory);
                return newBudgetCategory;
            });

            Optional<BudgetGoal> optionalBudgetGoal = chooseBudgetGoal(budgetCategory, goalRequest.getGoal().getTitle());
            if (!optionalBudgetGoal.isPresent()) {
                incrementBalanceForCurrency(budgetDetails.getBalance(), goalBalance.getCurrency(), goalBalance.getCompleteValue());
                incrementBalanceForCurrency(budgetCategory.getBalance(), goalBalance.getCurrency(), goalBalance.getCompleteValue());

                goalBalance.setValue(0d);
                BudgetGoal newBudgetGoal = new BudgetGoal(false, goalRequest.getGoal().getTitle(), goalBalance);
                budgetCategory.getGoals().add(newBudgetGoal);
            } else {
                LOG.warn(StringUtils.join("Goal '", goalRequest.getGoal().getTitle(), "' was not added to the period ", goalRequest.getActionPlan().getMonth(), ".", goalRequest.getActionPlan().getYear(), " because it is already present"));
                errors.add(StringUtils.join("Цель '", goalRequest.getGoal().getTitle(), "' не добавлена в период ", goalRequest.getActionPlan().getMonth(), ".", goalRequest.getActionPlan().getYear(), " потому что она там уже есть"));
            }
        });

        budgetRepository.saveAll(anotherBudgets.values());
        return errors;
    }


    private void updateBudgetDetailsBalanceBasedOnCategories(BudgetDetails budgetDetails) {
        Map<Currency, Double> completeValuesMap = budgetDetails.getCategories().stream()
                .map(BudgetCategory::getBalance)
                .flatMap(balanceMap -> balanceMap.entrySet().stream().map(balanceEntry -> new CurrencyBalanceValue(0d, balanceEntry.getValue().getCompleteValue(), balanceEntry.getKey())))
                .collect(Collectors.groupingBy(CurrencyBalanceValue::getCurrency, Collectors.summingDouble(CurrencyBalanceValue::getCompleteValue)));

        completeValuesMap.forEach((currency, completeValue) -> {
            Map<Currency, BalanceValue> balance = budgetDetails.getBalance();
            if (balance.containsKey(currency)) {
                BalanceValue balanceValue = balance.get(currency);
                if (balanceValue.getCompleteValue() < completeValue) {
                    balanceValue.setCompleteValue(completeValue);
                }
            } else {
                balance.put(currency, new BalanceValue(0d, completeValue));
            }
        });
    }

    private void incrementBalanceForCurrency(Map<Currency, BalanceValue> balance, Currency currency, Double incrementValue) {
        if (balance.containsKey(currency)) {
            BalanceValue balanceValue = balance.get(currency);
            balanceValue.setCompleteValue(balanceValue.getCompleteValue() + incrementValue);
        } else {
            balance.put(currency, new BalanceValue(0d, incrementValue));
        }
    }

    private void renameBudgetDetailsCategory(BudgetDetails budgetDetails, String oldCategoryTitle, String newCategoryTitle) {
        budgetDetails.getCategories().stream()
                .filter(category -> StringUtils.equals(oldCategoryTitle, category.getTitle()))
                .forEach(category -> category.setTitle(newCategoryTitle));
    }

    private void editBudgetGoalSameMonth(Update update, BudgetDetails budgetDetails, String originalGoalTitle, String goalTitle, BudgetCategory category, CurrencyBalanceValue balance, BudgetGoal originalGoal, HistoryType type, String categoryQuery, boolean changeGoalStatus) {
        if (!StringUtils.equals(originalGoalTitle, goalTitle) && category.getGoals().stream().anyMatch(budgetGoal -> StringUtils.equals(budgetGoal.getTitle(), goalTitle))) {
            throw new ItemAlreadyExistsException();
        }

        CurrencyBalanceValue originalGoalBalance = originalGoal.getBalance();
        Currency originalGoalBalanceCurrency = originalGoalBalance.getCurrency();
        Double balanceCompleteValue = balance.getCompleteValue();
        Double originalCurrencyChange = originalGoalBalance.getCompleteValue() * -1;
        Currency balanceCurrency = balance.getCurrency();

        String budgetBalanceQuery = type.name() + ".balance.";
        String categoryBalanceQuery = categoryQuery + ".balance.";
        String goalQuery = categoryQuery + ".goals." + category.getGoals().indexOf(originalGoal);
        String goalBalanceQuery = goalQuery + ".balance.";

//      If currency was not changed
        if (balanceCurrency.equals(originalGoalBalanceCurrency)) {
            originalCurrencyChange += balanceCompleteValue;
            update.inc(budgetBalanceQuery + originalGoalBalanceCurrency + ".completeValue", originalCurrencyChange);
            update.inc(categoryBalanceQuery + originalGoalBalanceCurrency + ".completeValue", originalCurrencyChange);
        } else {
            changeOriginalBudgetAndCategoryBalances(update, budgetDetails, category, originalGoalBalance, categoryQuery, type);
            update.inc(budgetBalanceQuery + balanceCurrency + ".value", 0d);
            update.inc(budgetBalanceQuery + balanceCurrency + ".completeValue", balanceCompleteValue);
            update.inc(categoryBalanceQuery + balanceCurrency + ".value", 0d);
            update.inc(categoryBalanceQuery + balanceCurrency + ".completeValue", balanceCompleteValue);
        }

        update.set(goalBalanceQuery + "completeValue", balanceCompleteValue);
        update.set(goalBalanceQuery + "currency", balanceCurrency);
        update.set(goalQuery + ".title", goalTitle);
        if (changeGoalStatus) {
            update.set(goalQuery + ".done", !originalGoal.isDone());
        }
    }

    private void changeOriginalBudgetAndCategoryBalances(Update update,BudgetDetails budgetDetails, BudgetCategory category, CurrencyBalanceValue originalGoalBalance, String categoryQuery, HistoryType type) {
        Currency currency = originalGoalBalance.getCurrency();
        Double completeValue = originalGoalBalance.getCompleteValue();
        Double originalCurrencyChange = completeValue * -1;

        String categoryBalanceQuery = categoryQuery + ".balance.";
        String categoryBalanceCurrencyQuery = categoryBalanceQuery + currency;
        String budgetBalanceQuery = type.name() + ".balance.";
        String budgetBalanceCurrencyQuery = budgetBalanceQuery + currency;

//      If category complete value = original balance complete value in similar currency
        if (category.getBalance().get(currency).getCompleteValue().equals(completeValue)) {
//          If this is the only currency for the category - remove category
            if (category.getBalance().size() == 1) {
                update.pull(type.name() + ".categories", Collections.singletonMap("title", category.getTitle()));
            } else {
                update.unset(categoryBalanceCurrencyQuery);
            }

//          If budget complete value = original balance complete value in similar currency
            if (budgetDetails.getBalance().get(currency).getCompleteValue().equals(completeValue)) {
                update.unset(budgetBalanceCurrencyQuery);
            } else {
                update.inc(budgetBalanceCurrencyQuery + ".completeValue", originalCurrencyChange);
            }
        } else {
            update.inc(categoryBalanceCurrencyQuery + ".completeValue", originalCurrencyChange);
            update.inc(budgetBalanceCurrencyQuery + ".completeValue", originalCurrencyChange);
        }
    }

    private Budget initEmptyMonthBudget(String login, int year, int month) {
        Budget budget = new Budget(login, year, month);
        return budgetRepository.save(budget);
    }

    private Budget validateAndGetBudget(String login, String budgetId) {
        Optional<Budget> optionalBudget = budgetRepository.findById(budgetId);
        if (!optionalBudget.isPresent()) {
            throw new BudgetMissedException(login, budgetId);
        }

        Budget budget = optionalBudget.get();
        if (!StringUtils.equals(login, budget.getUser())) {
            LOG.error(StringUtils.join("Budget with id=", budgetId, " is assigned to another user. Expected user=", login, ". Actual user=", budget.getUser()));
            throw new BudgetMissedException(login, budgetId);
        }

        return budget;
    }

    private BudgetDetails chooseBudgetDetails(Budget budget, HistoryType type) {
        switch (type) {
            case expense:
                return budget.getExpense();
            case income:
                return budget.getIncome();
            default:
                throw new UnsupportedBudgetTypeException(budget.getUser(), budget.getId(), type.name());
        }
    }

    private Optional<BudgetCategory> chooseBudgetCategory(BudgetDetails budgetDetails, String categoryTitle) {
        return budgetDetails.getCategories().stream()
                .filter(budgetCategory -> StringUtils.equals(budgetCategory.getTitle(), categoryTitle))
                .findFirst();
    }

    private BudgetCategory chooseBudgetCategory(BudgetDetails budgetDetails, String categoryTitle, String login, String budgetId) {
        return chooseBudgetCategory(budgetDetails, categoryTitle).orElseThrow(() -> new CategoryMissedException(login, budgetId, categoryTitle));
    }

    private Optional<BudgetGoal> chooseBudgetGoal(BudgetCategory budgetCategory, String goalTitle) {
        return budgetCategory.getGoals().stream()
                .filter(budgetGoal -> StringUtils.equals(budgetGoal.getTitle(), goalTitle))
                .findFirst();
    }

    private BudgetGoal chooseBudgetGoal(BudgetCategory budgetCategory, String goalTitle, String login, String budgetId) {
        return chooseBudgetGoal(budgetCategory, goalTitle).orElseThrow(() -> new GoalMissedException(login, budgetId, budgetCategory.getTitle(), goalTitle));
    }
}