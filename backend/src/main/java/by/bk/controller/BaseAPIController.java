package by.bk.controller;

import io.jsonwebtoken.ExpiredJwtException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Sergey Koval
 */
@RestController
@RequestMapping("/api")
public abstract class BaseAPIController {
    protected final Log LOG = LogFactory.getLog(this.getClass());

    @ExceptionHandler({ExpiredJwtException.class})
    public ResponseEntity<String> handleAuthenticationException(ExpiredJwtException e) {
        LOG.error(e);
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED.value()).body("EXPIRED");
    }
}