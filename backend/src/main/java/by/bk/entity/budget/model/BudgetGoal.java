package by.bk.entity.budget.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * @author Sergey Koval
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BudgetGoal {
    private boolean done;
    private String title;
    private CurrencyBalanceValue balance;
}