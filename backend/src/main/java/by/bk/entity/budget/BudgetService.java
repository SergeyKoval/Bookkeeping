package by.bk.entity.budget;

import by.bk.entity.budget.model.Budget;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * @author Sergey Koval
 */
@Service
public class BudgetService implements BudgetAPI {
    @Autowired
    private BudgetRepository budgetRepository;

    @Override
    public Budget getMonthBudget(String login, int year, int month) {
        Optional<Budget> budget = budgetRepository.findByUserAndYearAndMonth(login, year, month);
        return budget.orElseGet(() -> initEmptyMonthBudget(login, year, month));
    }

    private Budget initEmptyMonthBudget(String login, int year, int month) {
        Budget budget = new Budget(login, year, month);
        return budgetRepository.save(budget);
    }
}