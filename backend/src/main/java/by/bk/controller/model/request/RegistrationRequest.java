package by.bk.controller.model.request;

import lombok.Getter;

/**
 * @author Sergey Koval
 */
@Getter
public class RegistrationRequest {
    private String email;
    private String password;
    private String code;
}