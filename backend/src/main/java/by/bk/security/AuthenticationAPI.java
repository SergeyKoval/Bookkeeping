package by.bk.security;

import by.bk.controller.model.response.SimpleResponse;
import by.bk.entity.user.UserPermission;
import org.springframework.security.core.Authentication;

import java.util.Optional;
import java.util.function.Supplier;

/**
 * @author Sergey Koval
 */
public interface AuthenticationAPI {
    Authentication getAuthentication(String login, Optional<UserPermission> overridePermission);
    SimpleResponse sendRegistrationCode(String email, String password, boolean restorePassword);
    SimpleResponse reviewRegistrationCode(String email, String password, String code, Supplier<String> tokenSupplier);
}