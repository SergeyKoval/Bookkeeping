package by.bk.controller.model.request;

import by.bk.entity.user.UserPermission;
import lombok.Getter;

import java.util.List;

/**
 * @author Sergey Koval
 */
@Getter
public class UserChangeRequest {
    private String email;
    private String password;
    private List<UserPermission> roles;
}