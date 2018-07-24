package by.bk.entity.budget.model;

import by.bk.entity.currency.Currency;
import lombok.Getter;
import lombok.NoArgsConstructor;
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
@NoArgsConstructor
public class BudgetDetails {
    private boolean opened;
    private Map<Currency, BalanceValue> balance = new HashMap<>();
    private List<BudgetCategory> categories = new ArrayList<>();

    public BudgetDetails(boolean opened) {
        this.opened = opened;
    }
}