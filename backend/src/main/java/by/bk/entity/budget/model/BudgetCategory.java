package by.bk.entity.budget.model;

import by.bk.entity.currency.Currency;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Sergey Koval
 */
@Getter
@Setter
public class BudgetCategory {
    private String title;
    private Map<Currency, BalanceValue> balance = new HashMap<>();
    private List<BudgetGoal> goals = new ArrayList<>();
}