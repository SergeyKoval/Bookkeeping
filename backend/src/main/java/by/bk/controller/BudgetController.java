package by.bk.controller;

import by.bk.controller.model.request.BudgetCategoryRequest;
import by.bk.controller.model.request.BudgetCategoryStatisticsRequest;
import by.bk.controller.model.request.BudgetGoalRequest;
import by.bk.controller.model.request.BudgetRequest;
import by.bk.controller.model.response.SimpleResponse;
import by.bk.entity.budget.BudgetAPI;
import by.bk.entity.budget.exception.BudgetProcessException;
import by.bk.entity.budget.model.Budget;
import by.bk.entity.budget.model.BudgetStatistics;
import by.bk.entity.history.HistoryAPI;
import by.bk.entity.history.HistoryItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

/**
 * @author Sergey Koval
 */
@RestController
@RequestMapping("/api/budget")
public class BudgetController extends BaseAPIController {
    @Autowired
    private BudgetAPI budgetAPI;
    @Autowired
    private HistoryAPI historyAPI;

    @ExceptionHandler({BudgetProcessException.class})
    public SimpleResponse handleBudgetProcessException(BudgetProcessException e) {
        LOG.error(e);
        return SimpleResponse.fail();
    }

    @PostMapping
    public Budget getMonthBudget(@RequestBody BudgetRequest budgetRequest, Principal principal) {
        return budgetAPI.getMonthBudget(principal.getName(), budgetRequest.getYear(), budgetRequest.getMonth());
    }

    @PostMapping("/changeGoalDoneStatus")
    public SimpleResponse changeGoalDoneStatus(@RequestBody BudgetGoalRequest goalRequest, Principal principal) {
        return budgetAPI.changeGoalDoneStatus(principal.getName(), goalRequest.getBudgetId(), goalRequest.getType(), goalRequest.getCategory(), goalRequest.getGoal(), goalRequest.isDoneStatus());
    }

    @PostMapping("/toggleBudgetDetails")
    public SimpleResponse toggleBudgetDetails(@RequestBody BudgetRequest budgetRequest, Principal principal) {
        return budgetAPI.toggleBudgetDetails(principal.getName(), budgetRequest.getBudgetId(), budgetRequest.getType(), budgetRequest.isOpened());
    }

    @PostMapping("/addBudgetCategory")
    public SimpleResponse addBudgetCategory(@RequestBody BudgetCategoryRequest request, Principal principal) {
        return budgetAPI.addBudgetCategory(principal.getName(), request.getBudgetId(), request.getBudgetType(), request.getCategoryTitle(), request.getCurrencyBalance());
    }

    @PostMapping("/editBudgetCategory")
    public SimpleResponse editBudgetCategory(@RequestBody BudgetCategoryRequest request, Principal principal) {
        return budgetAPI.editBudgetCategory(principal.getName(), request.getBudgetId(), request.getBudgetType(), request.getCategoryTitle(), request.getCurrencyBalance());
    }

    @PostMapping("/addBudgetGoal")
    public SimpleResponse addBudgetGoal(@RequestBody BudgetGoalRequest request, Principal principal) {
        return budgetAPI.addBudgetGoal(principal.getName(), request.getBudgetId(), request.getYear(), request.getMonth(), request.getType(), request.getCategory(), request.getGoal(), request.getBalance());
    }

    @PostMapping("/editBudgetGoal")
    public SimpleResponse editBudgetGoal(@RequestBody BudgetGoalRequest request, Principal principal) {
        return budgetAPI.editBudgetGoal(principal.getName(), request.getBudgetId(), request.getYear(), request.getMonth(), request.getType(), request.getCategory(), request.getOriginalGoal(), request.getGoal(), request.getBalance(), request.isChangeGoalState());
    }

    @PostMapping("/updateBudgetLimit")
    public SimpleResponse updateBudgetLimit(@RequestBody BudgetRequest request, Principal principal) {
        return budgetAPI.updateBudgetLimit(principal.getName(), request.getBudgetId(), request.getType(), request.getBalance());
    }

    @PostMapping("/removeGoal")
    public SimpleResponse removeGoal(@RequestBody BudgetGoalRequest request, Principal principal) {
        return budgetAPI.removeGoal(principal.getName(), request.getBudgetId(), request.getType(), request.getCategory(), request.getGoal());
    }

    @PostMapping("/removeCategory")
    public SimpleResponse removeCategory(@RequestBody BudgetGoalRequest request, Principal principal) {
        return budgetAPI.removeCategory(principal.getName(), request.getBudgetId(), request.getType(), request.getCategory());
    }

    @PostMapping("/moveGoal")
    public SimpleResponse moveGoal(@RequestBody BudgetGoalRequest request, Principal principal) {
        return budgetAPI.moveGoal(principal.getName(), request.getBudgetId(), request.getYear(), request.getMonth(), request.getType(), request.getCategory(), request.getGoal(), request.getValue());
    }

    @GetMapping("/reviewBeforeRemoveHistoryItem/{historyItemId}")
    public SimpleResponse reviewBeforeRemoveHistoryItem(@PathVariable("historyItemId") String historyItemId, Principal principal) {
        String login = principal.getName();
        HistoryItem historyItem = historyAPI.getById(login, historyItemId);
        return budgetAPI.reviewBeforeRemoveHistoryItem(login, historyItem);
    }

    @PostMapping("/categoryStatistics")
    public List<BudgetStatistics> categoryStatistics(@RequestBody BudgetCategoryStatisticsRequest request, Principal principal) {
        return budgetAPI.categoryStatistics(principal.getName(), request);
    }
}