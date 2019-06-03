package by.bk.security.model;

import by.bk.entity.user.UserPermission;
import by.bk.security.JwtTokenUtil;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.apache.commons.lang3.EnumUtils;
import org.apache.commons.lang3.StringUtils;

import java.time.Instant;
import java.util.Optional;

/**
 * @author Sergey Koval
 */
public class JwtToken {
    private static final String TOKEN_SUFFIX = "\"";
    public static final String TOKEN_HEADER = "Authorization";
    public static final String TOKEN_PREFIX = "Bearer \"";

    private final DecodedJWT JWT;

    private JwtToken(DecodedJWT decodedJWT) {
        this.JWT = decodedJWT;
    }

    public static JwtToken from(String requestHeader, JwtTokenUtil tokenUtil) {
        final String authToken = StringUtils.substringBetween(requestHeader, TOKEN_PREFIX, TOKEN_SUFFIX);
        return new JwtToken(tokenUtil.getAllClaimsFromToken(authToken));
    }

    public boolean isExpired() {
        final Instant expiration = this.JWT.getExpiresAt().toInstant();
        return expiration.isBefore(Instant.now());
    }

    public String getUsername() {
        return this.JWT.getSubject();
    }

    public Optional<UserPermission> getAdditionalPermission() {
        return Optional.ofNullable(EnumUtils.getEnum(UserPermission.class, this.JWT.getClaim(JwtTokenUtil.ADDITIONAL_SCOPE).asString()));
    }
}