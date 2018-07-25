package by.bk.controller;

import by.bk.controller.model.request.BudgetGoalRequest;
import by.bk.controller.model.request.BudgetRequest;
import by.bk.controller.model.response.SimpleResponse;
import by.bk.entity.budget.BudgetAPI;
import by.bk.entity.budget.exception.BudgetProcessException;
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
    public Budget getMonthBudget(@RequestBody BudgetRequest budgetRequest, Principal principal) {
        return budgetAPI.getMonthBudget(principal.getName(), budgetRequest.getYear(), budgetRequest.getMonth());
    }

    @PostMapping("/changeGoalDoneStatus")
    public SimpleResponse changeGoalDoneStatus(@RequestBody BudgetGoalRequest goalRequest, Principal principal) {
        try {
            return budgetAPI.changeGoalDoneStatus(principal.getName(), goalRequest.getBudgetId(), goalRequest.getType(), goalRequest.getCategory(), goalRequest.getGoal(), goalRequest.isDoneStatus());
        } catch (BudgetProcessException e) {
            LOG.error(e);
            return SimpleResponse.fail();
        }
    }

    @PostMapping("/toggleBudgetDetails")
    public SimpleResponse toggleBudgetDetails(@RequestBody BudgetRequest budgetRequest, Principal principal) {
        try {
            return budgetAPI.toggleBudgetDetails(principal.getName(), budgetRequest.getBudgetId(), budgetRequest.getType(), budgetRequest.isOpened());
        } catch (BudgetProcessException e) {
            LOG.error(e);
            return SimpleResponse.fail();
        }
    }
}