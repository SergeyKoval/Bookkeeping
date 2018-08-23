package by.bk.controller.model.request;

import by.bk.entity.budget.model.CurrencyBalanceValue;
import by.bk.entity.history.HistoryType;
import lombok.Getter;

import java.util.List;

/**
 * @author Sergey Koval
 */
@Getter
public class BudgetRequest {
    private int year;
    private int month;
    private String budgetId;
    private HistoryType type;
    private boolean opened;
    private List<CurrencyBalanceValue> balance;
}