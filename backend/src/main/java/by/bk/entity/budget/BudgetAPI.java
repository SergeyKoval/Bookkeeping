package by.bk.entity.budget;

import by.bk.controller.model.response.SimpleResponse;
import by.bk.entity.budget.model.Budget;
import by.bk.entity.budget.model.CurrencyBalanceValue;
import by.bk.entity.history.HistoryType;

import java.util.List;

/**
 * @author Sergey Koval
 */
public interface BudgetAPI {
    Budget getMonthBudget(String login, int year, int month);
    SimpleResponse changeGoalDoneStatus(String login, String budgetId, HistoryType type, String categoryTitle, String goalTitle, boolean doneStatus);
    SimpleResponse toggleBudgetDetails(String login, String budgetId, HistoryType type, boolean opened);

    SimpleResponse addBudgetCategory(String login, String budgetId, HistoryType type, String categoryTitle, List<CurrencyBalanceValue> currencyBalances);
    SimpleResponse editBudgetCategory(String login, String budgetId, HistoryType type, String categoryTitle, List<CurrencyBalanceValue> currencyBalances);
    SimpleResponse addBudgetGoal(String login, String budgetId, Integer year, Integer month, HistoryType type, String categoryTitle, String goalTitle, CurrencyBalanceValue balance);
    SimpleResponse editBudgetGoal(String login, String budgetId, Integer year, Integer month, HistoryType type, String categoryTitle, String originalGoalTitle, String goalTitle, CurrencyBalanceValue balance, boolean changeGoalStatus);
    SimpleResponse updateBudgetLimit(String login, String budgetId, HistoryType type, List<CurrencyBalanceValue> currencyBalances);
}