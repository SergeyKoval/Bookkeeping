package by.bk.entity.budget;

import by.bk.controller.model.request.BudgetCategoryStatisticsRequest;
import by.bk.controller.model.request.BudgetCloseMonthRequest;
import by.bk.controller.model.response.SimpleResponse;
import by.bk.entity.budget.model.Budget;
import by.bk.entity.budget.model.BudgetStatistics;
import by.bk.entity.budget.model.CurrencyBalanceValue;
import by.bk.entity.history.HistoryItem;
import by.bk.entity.history.HistoryType;
import by.bk.entity.user.model.SubCategoryType;

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
    SimpleResponse removeGoal(String login, String budgetId, HistoryType type, String categoryTitle, String goalTitle);
    SimpleResponse moveCategory(String login, String oldCategoryTitle, String newCategoryTitle, String subCategoryTitle, SubCategoryType subCategoryType, List<HistoryItem> historyItems);
    SimpleResponse removeCategory(String login, String budgetId, HistoryType type, String categoryTitle);
    SimpleResponse moveGoal(String login, String budgetId, Integer year, Integer month, HistoryType type, String categoryTitle, String goalTitle, Double movedValue);

    SimpleResponse addHistoryItem(String login, HistoryItem historyItem, boolean changeGoalStatus);
    SimpleResponse deleteHistoryItem(String login, HistoryItem historyItem, boolean changeGoalStatus);
    SimpleResponse reviewBeforeRemoveHistoryItem(String login, HistoryItem historyItem);
    SimpleResponse editHistoryItem(String login, HistoryItem originalHistoryItem, boolean changeOriginalGoalStatus, HistoryItem historyItem, boolean changeGoalStatus);

    SimpleResponse renameCategory(String login, String oldCategoryTitle, String newCategoryTitle);

    List<BudgetStatistics> categoryStatistics(String login, BudgetCategoryStatisticsRequest request);
    SimpleResponse closeMonth(String login, BudgetCloseMonthRequest request);
}