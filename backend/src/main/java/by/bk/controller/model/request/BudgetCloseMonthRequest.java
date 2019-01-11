package by.bk.controller.model.request;

import lombok.Getter;

import java.util.List;

@Getter
public class BudgetCloseMonthRequest {
    private int year;
    private int month;
    private List<BudgetCloseMonthCategoryRequest> planingCategories;
    private List<BudgetCloseMonthGoalRequest> anotherMontGoals;
}