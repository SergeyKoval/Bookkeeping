package by.bk.entity.budget.exception;

import lombok.ToString;

/**
 * @author Sergey Koval
 */
@ToString
public class UnsupportedBudgetTypeException extends BudgetProcessException {
    private String type;

    public UnsupportedBudgetTypeException(String login, String budgetId, String type) {
        super(login, budgetId);
        this.type = type;
    }
}