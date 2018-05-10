package by.bk.controller;

import by.bk.security.JwtTokenUtil;
import by.bk.security.model.LoginRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.Collections;
import java.util.Map;

/**
 * @author Sergey Koval
 */
@RestController
@RequestMapping("/token")
public class AuthenticationController {
    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    private JwtTokenUtil tokenUtil;

    @ExceptionHandler({BadCredentialsException.class})
    public void handleAuthenticationException(HttpServletResponse response) {
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
    }

    @PostMapping("/generate-token")
    public Map<String, String> register(@RequestBody LoginRequest loginRequest) {
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword());
        authenticationManager.authenticate(authenticationToken);
        return Collections.singletonMap("token", tokenUtil.generateToken(loginRequest.getEmail()));
    }
}