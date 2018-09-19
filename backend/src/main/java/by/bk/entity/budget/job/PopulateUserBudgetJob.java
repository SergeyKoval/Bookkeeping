package by.bk.entity.budget.job;

import by.bk.entity.budget.BudgetRepository;
import by.bk.entity.budget.model.*;
import by.bk.entity.currency.Currency;
import by.bk.entity.history.Balance;
import by.bk.entity.history.HistoryItem;
import by.bk.entity.history.HistoryRepository;
import by.bk.entity.history.HistoryType;
import by.bk.entity.user.UserRepository;
import by.bk.entity.user.model.User;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author Sergey Koval
 */
@Component
public class PopulateUserBudgetJob {
    private static final DecimalFormat CURRENCY_FORMAT = new DecimalFormat("##0.00");

    @Autowired
    private BudgetRepository budgetRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private HistoryRepository historyRepository;

    public void populateBudgetForAllUsers() {
        List<User> users = userRepository.getAllUsers();
        users.forEach(user -> {
            Map<Integer, Map<Integer, Budget>> userBudgets = budgetRepository.findAllByUser(user.getEmail()).stream()
                    .peek(budget -> {
                        clearBudgetDetails(budget.getIncome());
                        clearBudgetDetails(budget.getExpense());
                    })
                    .collect(Collectors.groupingBy(Budget::getYear, Collectors.toMap(Budget::getMonth, budget -> budget)));
            historyRepository.getAllByUser(user.getEmail()).stream()
                    .filter(historyItem -> HistoryType.expense.equals(historyItem.getType()) || HistoryType.income.equals(historyItem.getType()))
                    .forEach(historyItem -> {
                        Map<Integer, Budget> budgetMap = userBudgets.get(historyItem.getYear());
                        Budget budget = budgetMap != null && budgetMap.containsKey(historyItem.getMonth())
                                ? budgetMap.get(historyItem.getMonth()) : initBudget(userBudgets, historyItem);
                        populateBudget(historyItem, budget);
                    });
            userBudgets.values().stream()
                    .flatMap(budgetMap -> budgetMap.values().stream())
                    .forEach(budget -> {
                        budgetRepository.save(budget);
                    });
        });
    }

    private Budget initBudget(Map<Integer, Map<Integer, Budget>> userBudgets, HistoryItem historyItem) {
        Map<Integer, Budget> budgetMap;
        int year = historyItem.getYear();
        int month = historyItem.getMonth();

        Budget budget = new Budget(historyItem.getUser(), year, month);
        if (userBudgets.containsKey(year)) {
            budgetMap = userBudgets.get(year);
        } else {
            budgetMap = new HashMap<>();
            userBudgets.put(year, budgetMap);
        }

        budgetMap.put(month, budget);
        return budget;
    }

    private void clearBudgetDetails(BudgetDetails budgetDetails) {
        budgetDetails.getBalance().forEach((currency, balanceValue) -> balanceValue.setValue(0d));
        budgetDetails.getCategories().removeAll(budgetDetails.getCategories().stream().filter(category -> StringUtils.isBlank(category.getTitle())).collect(Collectors.toList()));
        budgetDetails.getCategories().forEach(category -> {
            category.getBalance().forEach((currency, balanceValue) -> balanceValue.setValue(0d));
            category.getGoals().forEach(goal -> goal.getBalance().setValue(0d));
        });
    }

    private void populateBudget(HistoryItem historyItem, Budget budget) {
        final BudgetDetails budgetDetails = HistoryType.expense.equals(historyItem.getType()) ? budget.getExpense() : budget.getIncome();
        Balance historyBalance = historyItem.getBalance();
        Currency historyBalanceCurrency = historyBalance.getCurrency();
        populateBalance(budgetDetails.getBalance(), historyBalanceCurrency, historyBalance);

        BudgetCategory category = budgetDetails.getCategories().stream()
                .filter(categoryItem -> StringUtils.equals(categoryItem.getTitle(), historyItem.getCategory()))
                .findFirst()
                .orElseGet(() -> {
                    BudgetCategory newCategory = new BudgetCategory();
                    newCategory.setTitle(historyItem.getCategory());
                    budgetDetails.getCategories().add(newCategory);
                    return newCategory;
                });
        populateBalance(category.getBalance(), historyBalanceCurrency, historyBalance);

        if (StringUtils.isNotBlank(historyItem.getGoal())) {
            BudgetGoal goal = category.getGoals().stream()
                    .filter(goalItem -> StringUtils.equals(goalItem.getTitle(), historyItem.getGoal()))
                    .findFirst()
                    .orElseGet(() -> {
                        BudgetGoal newGoal = new BudgetGoal(false, historyItem.getGoal(), new CurrencyBalanceValue(0d, 0d, historyBalanceCurrency));
                        category.getGoals().add(newGoal);
                        return newGoal;
                    });
            CurrencyBalanceValue goalBalance = goal.getBalance();
            if (historyBalanceCurrency.equals(goalBalance.getCurrency())) {
                goalBalance.setValue(Double.parseDouble(CURRENCY_FORMAT.format(goalBalance.getValue() + historyBalance.getValue())));
            } else {
                goalBalance.setValue(Double.parseDouble(CURRENCY_FORMAT.format(goalBalance.getValue() + historyBalance.getAlternativeCurrency().get(goalBalance.getCurrency()))));
            }
        }
    }

    private void populateBalance(Map<Currency, BalanceValue> balance, Currency balanceCurrency, Balance historyBalance) {
        if (balance.containsKey(balanceCurrency)) {
            BalanceValue balanceValue = balance.get(balanceCurrency);
            if (balanceValue.getCompleteValue() == null) {
                balanceValue.setCompleteValue(0d);
            }
            balanceValue.setValue(Double.parseDouble(CURRENCY_FORMAT.format(balanceValue.getValue() + historyBalance.getValue())));
        } else {
            balance.put(balanceCurrency, new BalanceValue(historyBalance.getValue(), 0d));
        }
    }
}