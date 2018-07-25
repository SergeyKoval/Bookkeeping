package by.bk.entity.budget.exception;

import lombok.ToString;

/**
 * @author Sergey Koval
 */
@ToString
public class CategoryMissedException extends BudgetProcessException {
    private String categoryTitle;

    public CategoryMissedException(String login, String budgetId, String categoryTitle) {
        super(login, budgetId);
        this.categoryTitle = categoryTitle;
    }
}