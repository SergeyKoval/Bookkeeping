package by.bk.entity.budget;

import by.bk.controller.exception.ItemAlreadyExistsException;
import by.bk.controller.model.response.SimpleResponse;
import by.bk.entity.budget.exception.*;
import by.bk.entity.budget.model.*;
import by.bk.entity.currency.Currency;
import by.bk.entity.history.HistoryType;
import com.mongodb.client.result.UpdateResult;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
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
            if (usedValue > 0 && (!updateBalances.containsKey(currency) || updateBalances.get(currency).getCompleteValue() < usedValue)) {
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

        return updateResult.getModifiedCount() == 1 ? SimpleResponse.success() : SimpleResponse.fail();
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
                update.pull(categoryQuery + ".goals", originalGoal);
            }
            changeOriginalBudgetAndCategoryBalances(update, budgetDetails, category, originalGoalBalance, categoryQuery, type);
        }

        Query query = Query.query(Criteria.where("id").is(budgetId));
        UpdateResult updateResult = mongoTemplate.updateFirst(query, update, Budget.class);
        return updateResult.getModifiedCount() == 1 ? SimpleResponse.success() : SimpleResponse.fail();
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
                update.pull(type.name() + ".categories", category);
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

    private BudgetCategory chooseBudgetCategory(BudgetDetails budgetDetails, String categoryTitle, String login, String budgetId) {
        return budgetDetails.getCategories().stream()
                .filter(budgetCategory -> StringUtils.equals(budgetCategory.getTitle(), categoryTitle))
                .findFirst()
                .orElseThrow(() -> new CategoryMissedException(login, budgetId, categoryTitle));
    }

    private BudgetGoal chooseBudgetGoal(BudgetCategory budgetCategory, String goalTitle, String login, String budgetId) {
        return budgetCategory.getGoals().stream()
                .filter(budgetGoal -> StringUtils.equals(budgetGoal.getTitle(), goalTitle))
                .findFirst()
                .orElseThrow(() -> new GoalMissedException(login, budgetId, budgetCategory.getTitle(), goalTitle));
    }
}