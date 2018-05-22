package by.bk.entity.user;

import by.bk.controller.model.request.UpdateCurrencyRequest;
import by.bk.controller.model.response.SimpleResponse;
import by.bk.entity.currency.Currency;
import by.bk.entity.user.model.User;
import by.bk.security.AuthenticationAPI;

/**
 * @author Sergey Koval
 */
public interface UserAPI extends AuthenticationAPI {
    User getFullUserProfile(String login);
    SimpleResponse updateUserPassword(String login, String oldPassword, String newPassword);
    SimpleResponse includeCurrency(String login, Currency currency);
    SimpleResponse excludeCurrency(String login, Currency currency);
    SimpleResponse markCurrencyAsDefault(String login, Currency currency);
    SimpleResponse moveCurrency(String login, Currency currency, UpdateCurrencyRequest.Direction direction);
    SimpleResponse addAccount(String login, String title);
}