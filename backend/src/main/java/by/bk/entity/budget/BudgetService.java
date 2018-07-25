package by.bk.entity.budget;

import by.bk.controller.model.response.SimpleResponse;
import by.bk.entity.budget.exception.BudgetMissedException;
import by.bk.entity.budget.exception.CategoryMissedException;
import by.bk.entity.budget.exception.GoalMissedException;
import by.bk.entity.budget.exception.UnsupportedBudgetTypeException;
import by.bk.entity.budget.model.Budget;
import by.bk.entity.budget.model.BudgetCategory;
import by.bk.entity.budget.model.BudgetDetails;
import by.bk.entity.budget.model.BudgetGoal;
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

import java.util.Optional;

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
        BudgetCategory category = budgetDetails.getCategories().stream()
                .filter(budgetCategory -> StringUtils.equals(budgetCategory.getTitle(), categoryTitle))
                .findFirst()
                .orElseThrow(() -> new CategoryMissedException(login, budgetId, categoryTitle));
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
}