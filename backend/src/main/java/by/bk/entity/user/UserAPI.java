package by.bk.entity.user;

import by.bk.entity.user.model.User;
import by.bk.security.AuthenticationAPI;

/**
 * @author Sergey Koval
 */
public interface UserAPI extends AuthenticationAPI {
    User getFullUserProfile(String login);
}