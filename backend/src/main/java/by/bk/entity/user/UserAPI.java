package by.bk.entity.user;

import by.bk.controller.model.request.ChangeDeviceNameRequest;
import by.bk.controller.model.request.Direction;
import by.bk.controller.model.request.SubAccountAssignmentRequest;
import by.bk.controller.model.response.SimpleResponse;
import by.bk.entity.currency.Currency;
import by.bk.entity.history.Balance;
import by.bk.entity.history.HistoryType;
import by.bk.entity.user.model.Account;
import by.bk.entity.user.model.SubAccount;
import by.bk.entity.user.model.SubCategoryType;
import by.bk.entity.user.model.User;
import by.bk.security.AuthenticationAPI;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * @author Sergey Koval
 */
public interface UserAPI extends AuthenticationAPI {
    User getFullUserProfile(String login);
    List<Account> getAccountsSummary(String login, String deviceId);
    SimpleResponse updateUserPassword(String login, String oldPassword, String newPassword);

    SimpleResponse includeCurrency(String login, Currency currency);
    SimpleResponse excludeCurrency(String login, Currency currency);
    SimpleResponse markCurrencyAsDefault(String login, Currency currency);
    SimpleResponse moveCurrency(String login, Currency currency, Direction direction);

    SimpleResponse toggleAccount(String login, String accountTitle, boolean toggleState);
    SimpleResponse addAccount(String login, String title);
    SimpleResponse editAccount(String login, String newTitle, String oldTitle);
    Optional<Account> deleteAccount(String login, String title);
    SimpleResponse moveAccount(String login, String title, Direction direction);

    SimpleResponse addSubAccount(String login, String subAccountTitle, String accountTitle, String icon, Map<Currency, Double> balance, Boolean excludeFromTotals);
    Optional<SubAccount> changeSubAccountBalance(String login, String subAccountTitle, String accountTitle, Map<Currency, Double> balance);
    Optional<SubAccount> editSubAccount(String login, String accountTitle, String oldSubAccountTitle, String newSubAccountTitle, String icon, Map<Currency, Double> balance, Boolean excludeFromTotals);
    SimpleResponse moveSubAccount(String login, String accountTitle, String subAccountTitle, Direction direction);
    Optional<SubAccount> deleteSubAccount(String login, String accountTitle, String subAccountTitle);

    SimpleResponse addCategory(String login, String categoryTitle, String icon);
    SimpleResponse editCategory(String login, String oldCategoryTitle, String newCategoryTitle, String icon);
    SimpleResponse deleteCategory(String login, String categoryTitle);
    SimpleResponse moveCategory(String login, String categoryTitle, Direction direction);

    SimpleResponse addSubCategory(String login, String categoryTitle, String subCategoryTitle, SubCategoryType subCategoryType);
    SimpleResponse editSubCategory(String login, String categoryTitle, String oldSubCategoryTitle, String newSubCategoryTitle, SubCategoryType subCategoryType);
    SimpleResponse deleteSubCategory(String login, String categoryTitle, String subCategoryTitle, SubCategoryType subCategoryType);
    SimpleResponse moveSubCategory(String login, String categoryTitle, String subCategoryTitle, SubCategoryType subCategoryType, Direction direction);
    SimpleResponse moveSubCategoryToAnotherCategory(String login, String oldCategoryTitle, String newCategoryTitle, String subCategoryTitle, SubCategoryType subCategoryType);

    List<User> getAllUsers();
    SimpleResponse addUser(String email, String password, List<UserPermission> roles);
    SimpleResponse editUser(String email, String password, List<UserPermission> roles);
    SimpleResponse deleteUser(String email);

    SimpleResponse updateUserBalance(String login, HistoryType type, Balance historyBalance);

    SimpleResponse assignSubAccount(String login, String deviceId, SubAccountAssignmentRequest subAccountAssignment);
    SimpleResponse deassignSubAccount(String login, String deviceId, SubAccountAssignmentRequest subAccountAssignment);

    SimpleResponse changeDeviceName(String login, ChangeDeviceNameRequest deviceDetails);
    SimpleResponse logoutDevice(String login, String deviceId);
    SimpleResponse removeDevice(String login, String deviceId);

    SimpleResponse addTag(String login, String title, String color, String textColor);
    SimpleResponse editTag(String login, String oldTitle, String newTitle, String color, String textColor, Boolean active);
    SimpleResponse deleteTag(String login, String title);
}
