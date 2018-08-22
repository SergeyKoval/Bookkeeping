package by.bk.controller.model.request;

import by.bk.entity.budget.model.CurrencyBalanceValue;
import by.bk.entity.history.HistoryType;
import lombok.Getter;

/**
 * @author Sergey Koval
 */
@Getter
public class BudgetGoalRequest {
    private String budgetId;
    private Integer year;
    private Integer month;
    private HistoryType type;
    private String category;
    private String goal;
    private String originalGoal;
    private boolean doneStatus;
    private CurrencyBalanceValue balance;
    private boolean changeGoalState;
}