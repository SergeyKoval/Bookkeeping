package by.bk.controller;

import by.bk.entity.user.model.User;
import by.bk.entity.user.UserAPI;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

/**
 * @author Sergey Koval
 */
@RestController
@RequestMapping("/api/profile")
public class ProfileController extends BaseAPIController {
    @Autowired
    private UserAPI userAPI;

    @GetMapping("full")
    public User loadFullProfile(Principal principal) {
        User fullUserProfile = userAPI.getFullUserProfile(principal.getName());
        return fullUserProfile;
    }
}