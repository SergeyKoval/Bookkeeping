package by.bk.entity.budget;

import by.bk.entity.budget.model.Budget;

/**
 * @author Sergey Koval
 */
public interface BudgetAPI {
    Budget getMonthBudget(String login, int year, int month);
}