package by.bk.security.model;

import lombok.Getter;

/**
 * @author Sergey Koval
 */
@Getter
public class LoginRequest {
    private String email;
    private String password;
    private String deviceId;
}
