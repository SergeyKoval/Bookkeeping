package by.bk.entity.budget.exception;

import lombok.ToString;

/**
 * @author Sergey Koval
 */
@ToString
public class HistoryItemMissedException extends BudgetProcessException {
    private String historyItemId;

    public HistoryItemMissedException(String login, String historyItemId) {
        super(login, null);
    }
}