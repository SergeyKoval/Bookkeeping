package by.bk.entity.budget;

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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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
        BudgetGoal goal = category.getGoals().stream()
                .filter(budgetGoal -> StringUtils.equals(budgetGoal.getTitle(), goalTitle))
                .findFirst()
                .orElseThrow(() -> new GoalMissedException(login, budgetId, categoryTitle, goalTitle));
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
        Update update = new Update()
                .addToSet(type.name() + ".categories").value(category);
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
}