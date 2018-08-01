package by.bk.controller.model.request;

import by.bk.entity.budget.model.CurrencyBalanceValue;
import by.bk.entity.history.HistoryType;
import lombok.Getter;

import java.util.List;

/**
 * @author Sergey Koval
 */
@Getter
public class BudgetCategoryRequest {
    private String budgetId;
    private HistoryType budgetType;
    private String categoryTitle;
    private List<CurrencyBalanceValue> currencyBalance;
}