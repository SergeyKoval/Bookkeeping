package by.bk.entity.budget.exception;

import lombok.ToString;

/**
 * @author Sergey Koval
 */
@ToString
public class GoalMissedException extends BudgetProcessException {
    private String categoryTitle;
    private String goalTitle;

    public GoalMissedException(String login, String budgetId, String categoryTitle, String goalTitle) {
        super(login, budgetId);
        this.categoryTitle = categoryTitle;
        this.goalTitle = goalTitle;
    }
}