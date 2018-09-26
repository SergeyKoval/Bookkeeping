package by.bk.security;

import by.bk.controller.model.response.SimpleResponse;
import org.springframework.security.core.Authentication;

import java.util.function.Supplier;

/**
 * @author Sergey Koval
 */
public interface AuthenticationAPI {
    Authentication getAuthentication(String login);
    SimpleResponse sendRegistrationCode(String email, String password);
    SimpleResponse reviewRegistrationCode(String email, String password, String code, Supplier<String> tokenSupplier);
}