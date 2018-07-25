package by.bk.controller.model.request;

import by.bk.entity.history.HistoryType;
import lombok.Getter;

/**
 * @author Sergey Koval
 */
@Getter
public class BudgetGoalRequest {
    private String budgetId;
    private HistoryType type;
    private String category;
    private String goal;
    private boolean doneStatus;
}