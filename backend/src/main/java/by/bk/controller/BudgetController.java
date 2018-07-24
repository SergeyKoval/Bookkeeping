package by.bk.controller;

import by.bk.controller.model.request.BudgetRequest;
import by.bk.entity.budget.BudgetAPI;
import by.bk.entity.budget.model.Budget;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

/**
 * @author Sergey Koval
 */
@RestController
@RequestMapping("/api/budget")
public class BudgetController extends BaseAPIController {
    @Autowired
    private BudgetAPI budgetAPI;

    @PostMapping
    public Budget getMonthCurrencies(@RequestBody BudgetRequest currenciesRequest, Principal principal) {
        return budgetAPI.getMonthBudget(principal.getName(), currenciesRequest.getYear(), currenciesRequest.getMonth());
    }
}