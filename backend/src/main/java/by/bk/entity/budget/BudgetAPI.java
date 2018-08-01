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
}