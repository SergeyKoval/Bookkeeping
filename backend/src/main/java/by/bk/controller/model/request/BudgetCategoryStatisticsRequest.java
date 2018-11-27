package by.bk.controller.model.request;

import by.bk.entity.history.HistoryType;
import lombok.Getter;

/**
 * @author Sergey Koval
 */
@Getter
public class BudgetCategoryStatisticsRequest {
    private HistoryType budgetType;
    private String category;
    private int year;
    private int month;
}