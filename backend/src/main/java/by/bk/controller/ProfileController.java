package by.bk.controller;

import by.bk.controller.model.SimpleResponse;
import by.bk.controller.model.UserPasswordChangeRequest;
import by.bk.entity.user.exception.ChangingPasswordException;
import by.bk.entity.user.exception.PasswordMismatchException;
import by.bk.entity.user.model.User;
import by.bk.entity.user.UserAPI;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

/**
 * @author Sergey Koval
 */
@RestController
@RequestMapping("/api/profile")
public class ProfileController extends BaseAPIController {
    @Autowired
    private UserAPI userAPI;

    @ExceptionHandler({PasswordMismatchException.class})
    public SimpleResponse handlePasswordMismatchException() {
        return SimpleResponse.fail("INVALID_PASSWORD");
    }

    @ExceptionHandler({ChangingPasswordException.class})
    public SimpleResponse handleChangingPasswordException() {
        return SimpleResponse.fail("ERROR");
    }

    @GetMapping("/full")
    public User loadFullProfile(Principal principal) {
        User fullUserProfile = userAPI.getFullUserProfile(principal.getName());
        return fullUserProfile;
    }

    @PostMapping("/change-password")
    public SimpleResponse changeUserPassword(@RequestBody UserPasswordChangeRequest request, Principal principal) {
        userAPI.updateUserPassword(principal.getName(), request.getOldPassword(), request.getNewPassword());
        return SimpleResponse.success();
    }
}