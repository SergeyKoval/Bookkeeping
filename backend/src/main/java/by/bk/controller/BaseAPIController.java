package by.bk.controller;

import by.bk.controller.exception.ItemAlreadyExistsException;
import by.bk.controller.model.response.SimpleResponse;
import by.bk.security.model.JwtUser;
import by.bk.security.role.RoleUser;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

/**
 * @author Sergey Koval
 */
@RoleUser
@RestController
@RequestMapping("/api")
public abstract class BaseAPIController {
    protected final Log LOG = LogFactory.getLog(this.getClass());

    @ExceptionHandler({ItemAlreadyExistsException.class})
    public SimpleResponse handleBudgetProcessException(ItemAlreadyExistsException e) {
        return SimpleResponse.alreadyExistsFail();
    }

    protected String getDeviceId(Principal principal) {
        UsernamePasswordAuthenticationToken token = (UsernamePasswordAuthenticationToken) principal;
        JwtUser jwtUser = (JwtUser) token.getPrincipal();
        return jwtUser.getDeviceId();
    }
}
