package by.bk.entity.user;

import by.bk.controller.model.request.Direction;
import by.bk.controller.model.response.SimpleResponse;
import by.bk.entity.currency.Currency;
import by.bk.entity.user.model.User;
import by.bk.security.AuthenticationAPI;

import java.util.Map;

/**
 * @author Sergey Koval
 */
public interface UserAPI extends AuthenticationAPI {
    User getFullUserProfile(String login);
    SimpleResponse updateUserPassword(String login, String oldPassword, String newPassword);

    SimpleResponse includeCurrency(String login, Currency currency);
    SimpleResponse excludeCurrency(String login, Currency currency);
    SimpleResponse markCurrencyAsDefault(String login, Currency currency);
    SimpleResponse moveCurrency(String login, Currency currency, Direction direction);

    SimpleResponse toggleAccount(String login, String accountTitle, boolean toggleState);
    SimpleResponse addAccount(String login, String title);
    SimpleResponse editAccount(String login, String newTitle, String oldTitle);
    SimpleResponse deleteAccount(String login, String title);
    SimpleResponse moveAccount(String login, String title, Direction direction);

    SimpleResponse addSubAccount(String login, String subAccountTitle, String accountTitle, String icon, Map<Currency, Double> balance);
    SimpleResponse changeSubAccountBalance(String login, String subAccountTitle, String accountTitle, Map<Currency, Double> balance);
    SimpleResponse editSubAccount(String login, String accountTitle, String oldSubAccountTitle, String newSubAccountTitle, String icon, Map<Currency, Double> balance);
    SimpleResponse moveSubAccount(String login, String accountTitle, String subAccountTitle, Direction direction);
}