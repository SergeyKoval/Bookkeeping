package by.bk.controller;

import by.bk.controller.model.request.UpdateCurrencyRequest;
import by.bk.controller.model.response.SimpleResponse;
import by.bk.controller.model.request.UserPasswordChangeRequest;
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

    @GetMapping("/full")
    public User loadFullProfile(Principal principal) {
        return userAPI.getFullUserProfile(principal.getName());
    }

    @PostMapping("/change-password")
    public SimpleResponse changeUserPassword(@RequestBody UserPasswordChangeRequest request, Principal principal) {
        return userAPI.updateUserPassword(principal.getName(), request.getOldPassword(), request.getNewPassword());
    }

    @PostMapping("/update-user-currency")
    public SimpleResponse updateProfileCurrency(@RequestBody UpdateCurrencyRequest request, Principal principal) {
        return request.getUse()
                ? userAPI.addCurrencyToUser(principal.getName(), request.getName())
                : userAPI.removeCurrencyFromUser(principal.getName(), request.getName());
    }

    @PostMapping("/update-user-currency-default")
    public SimpleResponse updateProfileCurrencyDefault(@RequestBody UpdateCurrencyRequest request, Principal principal) {
        return userAPI.markCurrencyAsDefault(principal.getName(), request.getName());
    }
}