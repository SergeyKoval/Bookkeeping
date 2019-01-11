package by.bk.controller.model.request;

import by.bk.entity.budget.model.BalanceValue;
import by.bk.entity.budget.model.BudgetCategory;
import by.bk.entity.currency.Currency;
import by.bk.entity.history.HistoryType;
import lombok.Getter;

import java.util.List;
import java.util.Map;

@Getter
public class BudgetCloseMonthCategoryRequest {
    private BudgetCategory category;
    private HistoryType type;
    private Map<Currency, BalanceValue> actionPlan;
    private List<BudgetCloseMonthGoalRequest> goalWrappers;
}