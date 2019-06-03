package by.bk.controller;

import by.bk.controller.model.request.RegistrationRequest;
import by.bk.controller.model.response.SimpleResponse;
import by.bk.security.AuthenticationAPI;
import by.bk.security.JwtTokenUtil;
import by.bk.security.MissedUserException;
import by.bk.security.model.LoginRequest;
import by.bk.security.role.RoleUser;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
    private static final Log LOG = LogFactory.getLog(AuthenticationController.class);
    private static final String BAD_CREDENTIALS = "BAD CREDENTIALS";
    private static final String MISSED_USER = "MISSED USER";

    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    private JwtTokenUtil tokenUtil;
    @Autowired
    private AuthenticationAPI authenticationAPI;
    @Value("${project.version}")
    private String version;

    @ExceptionHandler({BadCredentialsException.class})
    public ResponseEntity<String> handleAuthenticationException() {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED.value()).body(BAD_CREDENTIALS);
    }

    @ExceptionHandler({MissedUserException.class})
    public ResponseEntity<String> handleMissedUserException(MissedUserException e) {
        LOG.error("Missed user: " + e.toString());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED.value()).body(MISSED_USER);
    }

    @GetMapping("/server/version")
    public SimpleResponse getServerVersion() {
        return SimpleResponse.success(version);
    }

    @PostMapping("/generate-token")
    public Map<String, String> register(@RequestBody LoginRequest loginRequest) {
        String email = StringUtils.lowerCase(loginRequest.getEmail());
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(email, loginRequest.getPassword());
        authenticationManager.authenticate(authenticationToken);
        return Collections.singletonMap("token", tokenUtil.generateToken(email, loginRequest.getScope()));
    }

    @PostMapping("/send-registration-code")
    public SimpleResponse sendRegistrationCode(@RequestBody RegistrationRequest request) {
        String email = StringUtils.lowerCase(request.getEmail());
        return authenticationAPI.sendRegistrationCode(email, request.getPassword(), request.isRestorePassword());
    }

    @PostMapping("/review-registration-code")
    public SimpleResponse reviewRegistrationCode(@RequestBody RegistrationRequest request) {
        String email = StringUtils.lowerCase(request.getEmail());
        return authenticationAPI.reviewRegistrationCode(email, request.getPassword(), request.getCode(), () -> tokenUtil.generateToken(email));
    }

    @RoleUser
    @PostMapping("/refresh-token")
    public Map<String, String> refresh(Principal principal) {
        return Collections.singletonMap("token", tokenUtil.generateToken(principal.getName()));
    }
}