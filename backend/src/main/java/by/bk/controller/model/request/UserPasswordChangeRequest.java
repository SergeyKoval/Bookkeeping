package by.bk.controller.model.request;

import lombok.Getter;

/**
 * @author Sergey Koval
 */
@Getter
public class UserPasswordChangeRequest {
    private String oldPassword;
    private String newPassword;
}