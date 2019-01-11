package by.bk.controller.model.request;

import by.bk.entity.budget.model.CurrencyBalanceValue;
import lombok.Getter;

@Getter
public class BudgetCloseMonthGoalPlanRequest {
    private CurrencyBalanceValue balance;
    private int year;
    private int month;
}