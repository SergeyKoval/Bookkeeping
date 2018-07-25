package by.bk.entity.budget.exception;

import lombok.ToString;

/**
 * @author Sergey Koval
 */
@ToString
public class BudgetMissedException extends BudgetProcessException {
    public BudgetMissedException(String login, String budgetId) {
        super(login, budgetId);
    }
}