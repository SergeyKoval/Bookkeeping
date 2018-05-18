package by.bk.controller.model;

import lombok.Getter;

/**
 * @author Sergey Koval
 */
@Getter
public class UserPasswordChangeRequest {
    private String oldPassword;
    private String newPassword;
}