package by.bk.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTCreator;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

/**
 * @author Sergey Koval
 */
@Component
public class JwtTokenUtil {
    public static final String ADDITIONAL_SCOPE = "additionalScope";
    private static final String ISSUER = "https://deplake.tk";

    @Value("${jwt.secret}")
    private String secret;
    @Value("${jwt.expiration}")
    private long expiration;

    public DecodedJWT getAllClaimsFromToken(String token) {
        return JWT.decode(token);
    }

    public String generateToken(String subject, String scope) {
        Instant now = Instant.now();
        JWTCreator.Builder builder = JWT.create()
                .withSubject(subject)
                .withIssuer(ISSUER)
                .withIssuedAt(Date.from(now))
                .withExpiresAt(Date.from(now.plus(expiration, ChronoUnit.MINUTES)));
        if (StringUtils.isNotBlank(scope)) {
            builder.withClaim(ADDITIONAL_SCOPE, scope);
        }
        return builder.sign(Algorithm.HMAC256(secret));
    }

    public String generateToken(String subject) {
        return generateToken(subject, null);
    }
}