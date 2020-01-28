package by.bk.security.model;

import by.bk.security.JwtTokenUtil;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.apache.commons.lang3.StringUtils;

import java.time.Instant;

/**
 * @author Sergey Koval
 */
public class JwtToken {
    private static final String TOKEN_SUFFIX = "\"";
    public static final String TOKEN_HEADER = "Authorization";
    public static final String TOKEN_PREFIX = "Bearer ";

    private final DecodedJWT JWT;
    private final String TOKEN;

    private JwtToken(String token, DecodedJWT decodedJWT) {
        this.JWT = decodedJWT;
        this.TOKEN = token;
    }

    public static JwtToken from(String requestHeader, JwtTokenUtil tokenUtil) {
        String authToken = StringUtils.substringAfter(requestHeader, TOKEN_PREFIX);
        authToken = StringUtils.contains(authToken, TOKEN_SUFFIX) ? StringUtils.substringBetween(authToken, TOKEN_SUFFIX) : authToken;
        return new JwtToken(authToken, tokenUtil.getAllClaimsFromToken(authToken));
    }

    public boolean isExpired() {
        final Instant expiration = this.JWT.getExpiresAt().toInstant();
        return expiration.isBefore(Instant.now());
    }

    public String getUsername() {
        return this.JWT.getSubject();
    }

    public String getDeviceId() {
        return this.JWT.getClaim(JwtTokenUtil.DEVICE_ID).asString();
    }

    public String getToken() {
        return TOKEN;
    }
}
