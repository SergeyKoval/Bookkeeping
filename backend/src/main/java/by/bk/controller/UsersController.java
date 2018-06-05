package by.bk.controller;

import by.bk.controller.model.request.UserChangeRequest;
import by.bk.controller.model.response.SimpleResponse;
import by.bk.entity.user.UserAPI;
import by.bk.entity.user.model.User;
import by.bk.security.role.RoleAdmin;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

/**
 * @author Sergey Koval
 */
@RoleAdmin
@RestController
@RequestMapping("/api/users")
public class UsersController extends BaseAPIController {
    @Autowired
    private UserAPI userAPI;

    @GetMapping("/all")
    public List<User> loadFullProfile() {
        return userAPI.getAllUsers();
    }

    @PostMapping("/add")
    public SimpleResponse addUser(@RequestBody UserChangeRequest request) {
        return this.userAPI.addUser(request.getEmail(), request.getPassword(), request.getRoles());
    }

    @PostMapping("/edit")
    public SimpleResponse editUser(@RequestBody UserChangeRequest request) {
        return this.userAPI.editUser(request.getEmail(), request.getPassword(), request.getRoles());
    }

    @PostMapping("/delete")
    public SimpleResponse deleteUser(@RequestBody UserChangeRequest request, Principal principal) {
        if (StringUtils.equals(request.getEmail(), principal.getName())) {
            LOG.warn("Trying to remove current user {} which is not allowed.");
            return SimpleResponse.fail();
        }
        return this.userAPI.deleteUser(request.getEmail());
    }
}