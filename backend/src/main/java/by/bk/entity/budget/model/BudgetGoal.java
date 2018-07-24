package by.bk.entity.budget.model;

import lombok.Getter;
import lombok.Setter;

/**
 * @author Sergey Koval
 */
@Getter
@Setter
public class BudgetGoal {
    private boolean done;
    private String title;
    private CurrencyBalanceValue balance;
}