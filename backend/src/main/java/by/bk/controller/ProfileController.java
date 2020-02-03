package by.bk.controller;

import by.bk.controller.model.request.SubAccountAssignmentRequest;
import by.bk.controller.model.request.UpdateAccountCategoryRequest;
import by.bk.controller.model.request.UpdateCurrencyRequest;
import by.bk.controller.model.response.SimpleResponse;
import by.bk.controller.model.request.UserPasswordChangeRequest;
import by.bk.entity.user.exception.SelectableItemMissedSettingUpdateException;
import by.bk.entity.user.model.Account;
import by.bk.entity.user.model.User;
import by.bk.entity.user.UserAPI;
import by.bk.security.role.RoleMobile;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

/**
 * @author Sergey Koval
 */
@RestController
@RequestMapping("/api/profile")
public class ProfileController extends BaseAPIController {
    @Autowired
    private UserAPI userAPI;

    @ExceptionHandler({SelectableItemMissedSettingUpdateException.class})
    public SimpleResponse handleAuthenticationException(SelectableItemMissedSettingUpdateException e) {
        LOG.error(e.getErrorMessage());
        return e.getSimpleResponse();
    }

    @GetMapping("/full")
    public User loadFullProfile(Principal principal) {
        return userAPI.getFullUserProfile(principal.getName());
    }

    @PostMapping("/change-password")
    public SimpleResponse changeUserPassword(@RequestBody UserPasswordChangeRequest request, Principal principal) {
        return userAPI.updateUserPassword(principal.getName(), request.getOldPassword(), request.getNewPassword());
    }

    @PostMapping("/update-user-currency")
    public SimpleResponse updateProfileCurrency(@RequestBody UpdateCurrencyRequest request, Principal principal) {
        return request.getUse()
                ? userAPI.includeCurrency(principal.getName(), request.getName())
                : userAPI.excludeCurrency(principal.getName(), request.getName());
    }

    @PostMapping("/update-user-currency-default")
    public SimpleResponse updateProfileCurrencyDefault(@RequestBody UpdateCurrencyRequest request, Principal principal) {
        return userAPI.markCurrencyAsDefault(principal.getName(), request.getName());
    }

    @PostMapping("/update-user-currency-move")
    public SimpleResponse updateProfileMoveCurrency(@RequestBody UpdateCurrencyRequest request, Principal principal) {
        return userAPI.moveCurrency(principal.getName(), request.getName(), request.getDirection());
    }

    @RoleMobile
    @GetMapping("/accounts")
    public List<Account> getAccounts(Principal principal) {
        return userAPI.getAccountsSummary(principal.getName(), getDeviceId(principal));
    }

    @RoleMobile
    @PostMapping("/assign-sub-account")
    public SimpleResponse assignSubAccount(@RequestBody SubAccountAssignmentRequest request, Principal principal) {
        return userAPI.assignSubAccount(principal.getName(), getDeviceId(principal), request);
    }

    @RoleMobile
    @PostMapping("/deassign-sub-account")
    public SimpleResponse deassignSubAccount(@RequestBody SubAccountAssignmentRequest request, Principal principal) {
        return userAPI.deassignSubAccount(principal.getName(), getDeviceId(principal), request);
    }

    @PostMapping("/add-account")
    public SimpleResponse addAccount(@RequestBody UpdateAccountCategoryRequest request, Principal principal) {
        return userAPI.addAccount(principal.getName(), request.getTitle());
    }

    @PostMapping("/edit-account")
    public SimpleResponse editAccount(@RequestBody UpdateAccountCategoryRequest request, Principal principal) {
        if (StringUtils.equals(request.getTitle(), request.getOldTitle())) {
            return SimpleResponse.success();
        }
        return userAPI.editAccount(principal.getName(), request.getTitle(), request.getOldTitle());
    }

    @PostMapping("/delete-account")
    public SimpleResponse deleteAccount(@RequestBody UpdateAccountCategoryRequest request, Principal principal) {
        return userAPI.deleteAccount(principal.getName(), request.getTitle());
    }

    @PostMapping("/move-account")
    public SimpleResponse moveAccount(@RequestBody UpdateAccountCategoryRequest request, Principal principal) {
        return userAPI.moveAccount(principal.getName(), request.getTitle(), request.getDirection());
    }

    @PostMapping("/add-sub-account")
    public SimpleResponse addSubAccount(@RequestBody UpdateAccountCategoryRequest request, Principal principal) {
        return userAPI.addSubAccount(principal.getName(), request.getTitle(), request.getParentTitle(), request.getIcon(), request.getBalance());
    }

    @PostMapping("/change-sub-account-balance")
    public SimpleResponse changeSubAccountBalance(@RequestBody UpdateAccountCategoryRequest request, Principal principal) {
        return userAPI.changeSubAccountBalance(principal.getName(), request.getTitle(), request.getParentTitle(), request.getBalance());
    }

    @PostMapping("/edit-sub-account")
    public SimpleResponse editSubAccount(@RequestBody UpdateAccountCategoryRequest request, Principal principal) {
        return userAPI.editSubAccount(principal.getName(), request.getParentTitle(), request.getOldTitle(), request.getTitle(), request.getIcon(), request.getBalance());
    }

    @PostMapping("/move-sub-account")
    public SimpleResponse moveSubAccount(@RequestBody UpdateAccountCategoryRequest request, Principal principal) {
        return userAPI.moveSubAccount(principal.getName(), request.getParentTitle(), request.getTitle(), request.getDirection());
    }

    @PostMapping("/toggle-account")
    public SimpleResponse toggleAccount(@RequestBody UpdateAccountCategoryRequest request, Principal principal) {
        return userAPI.toggleAccount(principal.getName(), request.getTitle(), request.isToggleState());
    }

    @PostMapping("/delete-sub-account")
    public SimpleResponse deleteSubAccount(@RequestBody UpdateAccountCategoryRequest request, Principal principal) {
        return userAPI.deleteSubAccount(principal.getName(), request.getParentTitle(), request.getTitle());
    }

    @PostMapping("/add-category")
    public SimpleResponse addCategory(@RequestBody UpdateAccountCategoryRequest request, Principal principal) {
        return userAPI.addCategory(principal.getName(), request.getTitle(), request.getIcon());
    }

    @PostMapping("/edit-category")
    public SimpleResponse editCategory(@RequestBody UpdateAccountCategoryRequest request, Principal principal) {
        return userAPI.editCategory(principal.getName(), request.getOldTitle(), request.getTitle(), request.getIcon());
    }

    @PostMapping("/delete-category")
    public SimpleResponse deleteCategory(@RequestBody UpdateAccountCategoryRequest request, Principal principal) {
        return userAPI.deleteCategory(principal.getName(), request.getTitle());
    }

    @PostMapping("/move-category")
    public SimpleResponse moveCategory(@RequestBody UpdateAccountCategoryRequest request, Principal principal) {
        return userAPI.moveCategory(principal.getName(), request.getTitle(), request.getDirection());
    }

    @PostMapping("/add-sub-category")
    public SimpleResponse addSubCategory(@RequestBody UpdateAccountCategoryRequest request, Principal principal) {
        return userAPI.addSubCategory(principal.getName(), request.getParentTitle(), request.getTitle(), request.getSubCategoryType());
    }

    @PostMapping("/edit-sub-category")
    public SimpleResponse editSubCategory(@RequestBody UpdateAccountCategoryRequest request, Principal principal) {
        if (StringUtils.equals(request.getTitle(), request.getOldTitle())) {
            return SimpleResponse.success();
        }
        return userAPI.editSubCategory(principal.getName(), request.getParentTitle(), request.getOldTitle(), request.getTitle(), request.getSubCategoryType());
    }

    @PostMapping("/delete-sub-category")
    public SimpleResponse deleteSubCategory(@RequestBody UpdateAccountCategoryRequest request, Principal principal) {
        return userAPI.deleteSubCategory(principal.getName(), request.getParentTitle(), request.getTitle(), request.getSubCategoryType());
    }

    @PostMapping("/move-sub-category")
    public SimpleResponse moveSubCategory(@RequestBody UpdateAccountCategoryRequest request, Principal principal) {
        return userAPI.moveSubCategory(principal.getName(), request.getParentTitle(), request.getTitle(), request.getSubCategoryType(), request.getDirection());
    }

    @PostMapping("/move-sub-category-to-another-category")
    public SimpleResponse moveSubCategoryToAnotherCategory(@RequestBody UpdateAccountCategoryRequest request, Principal principal) {
        return userAPI.moveSubCategoryToAnotherCategory(principal.getName(), request.getOldTitle(), request.getParentTitle(), request.getTitle(), request.getSubCategoryType());
    }
}
