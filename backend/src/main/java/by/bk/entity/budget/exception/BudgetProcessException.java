package by.bk.entity.budget.exception;

import lombok.AllArgsConstructor;

/**
 * @author Sergey Koval
 */
@AllArgsConstructor
public class BudgetProcessException extends RuntimeException {
    protected String login;
    protected String budgetId;
}