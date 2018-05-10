package by.bk.security;

import org.springframework.security.core.Authentication;

/**
 * @author Sergey Koval
 */
public interface AuthenticationAPI {
    Authentication getAuthentication(String username);
}