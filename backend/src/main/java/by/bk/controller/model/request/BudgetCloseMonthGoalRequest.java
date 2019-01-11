package by.bk.controller.model.request;

import by.bk.entity.budget.model.BudgetGoal;
import by.bk.entity.history.HistoryType;
import lombok.Getter;

@Getter
public class BudgetCloseMonthGoalRequest {
    private String category;
    private HistoryType type;
    private BudgetGoal goal;
    private BudgetCloseMonthGoalPlanRequest actionPlan;
}