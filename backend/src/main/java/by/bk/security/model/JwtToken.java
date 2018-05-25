package by.bk.security.model;

import by.bk.security.JwtTokenUtil;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import org.apache.commons.lang3.StringUtils;

import java.time.Instant;

/**
 * @author Sergey Koval
 */
public class JwtToken {
    private static final String TOKEN_SUFFIX = "\"";
    public static final String TOKEN_HEADER = "Authorization";
    public static final String TOKEN_PREFIX = "Bearer \"";

    private final Claims CLAIMS;

    private JwtToken(Claims claims) {
        this.CLAIMS = claims;
    }

    public static JwtToken from(String requestHeader, JwtTokenUtil tokenUtil) throws ExpiredJwtException {
        final String authToken = StringUtils.substringBetween(requestHeader, TOKEN_PREFIX, TOKEN_SUFFIX);
        return new JwtToken(tokenUtil.getAllClaimsFromToken(authToken));
    }

    public boolean isExpired() {
        final Instant expiration = this.CLAIMS.getExpiration().toInstant();
        return expiration.isBefore(Instant.now());
    }

    public String getUsername() {
        return this.CLAIMS.getSubject();
    }
}