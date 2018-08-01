package by.bk.entity.budget.exception;

import by.bk.entity.currency.Currency;
import lombok.ToString;

/**
 * @author Sergey Koval
 */
@ToString
public class InvalidBudgetPlanningException extends BudgetProcessException {
    private String categoryTitle;
    private Currency currency;
    private Double usedValue;
    private Double updateCompleteValue;

    public InvalidBudgetPlanningException(String login, String budgetId, String categoryTitle, Currency currency, Double usedValue, Double updateCompleteValue) {
        super(login, budgetId);
        this.categoryTitle = categoryTitle;
        this.currency = currency;
        this.usedValue = usedValue;
        this.updateCompleteValue = updateCompleteValue;
    }
}