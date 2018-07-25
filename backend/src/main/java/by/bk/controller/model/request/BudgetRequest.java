package by.bk.controller.model.request;

import by.bk.entity.history.HistoryType;
import lombok.Getter;

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
}