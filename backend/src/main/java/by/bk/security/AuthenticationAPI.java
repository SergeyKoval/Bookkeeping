package by.bk.security;

import by.bk.controller.model.response.SimpleResponse;
import by.bk.security.model.JwtToken;
import org.springframework.security.core.Authentication;

import java.util.function.Supplier;

/**
 * @author Sergey Koval
 */
public interface AuthenticationAPI {
    Authentication getAuthentication(JwtToken token);
    SimpleResponse sendRegistrationCode(String email, String password, boolean restorePassword);
    SimpleResponse reviewRegistrationCode(String email, String password, String code, Supplier<String> tokenSupplier);
    void registerDevice(String email, String deviceId, String token);
}
