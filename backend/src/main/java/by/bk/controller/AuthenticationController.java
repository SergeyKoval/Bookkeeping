package by.bk.controller;

import by.bk.security.JwtTokenUtil;
import by.bk.security.model.LoginRequest;
import by.bk.security.role.RoleUser;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Collections;
import java.util.Map;

/**
 * @author Sergey Koval
 */
@RestController
@RequestMapping("/token")
public class AuthenticationController {
    private static final String BAD_CREDENTIALS = "BAD CREDENTIALS";

    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    private JwtTokenUtil tokenUtil;

    @ExceptionHandler({BadCredentialsException.class})
    public ResponseEntity<String> handleAuthenticationException() {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED.value()).body(BAD_CREDENTIALS);
    }

    @PostMapping("/generate-token")
    public Map<String, String> register(@RequestBody LoginRequest loginRequest) {
        String email = StringUtils.lowerCase(loginRequest.getEmail());
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(email, loginRequest.getPassword());
        authenticationManager.authenticate(authenticationToken);
        return Collections.singletonMap("token", tokenUtil.generateToken(email));
    }

    @RoleUser
    @PostMapping("/refresh-token")
    public Map<String, String> refresh(Principal principal) {
        return Collections.singletonMap("token", tokenUtil.generateToken(principal.getName()));
    }
}