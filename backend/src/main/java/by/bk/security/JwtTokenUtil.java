package by.bk.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
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
    public static final String DEVICE_ID = "deviceId";
    private static final String ISSUER = "https://deplake.tk";

    @Value("${jwt.secret}")
    private String secret;
    @Value("${jwt.expiration}")
    private long expiration;
    @Value("${jwt.mobile.expiration}")
    private long mobileExpiration;

    public DecodedJWT getAllClaimsFromToken(String token) {
        return JWT.decode(token);
    }

    public String generateToken(String subject) {
        Instant now = Instant.now();
        return JWT.create().withSubject(subject)
                .withIssuer(ISSUER)
                .withIssuedAt(Date.from(now))
                .withExpiresAt(Date.from(now.plus(expiration, ChronoUnit.MINUTES)))
                .sign(Algorithm.HMAC256(secret));
    }

    public String generateTokenMobile(String subject, String deviceId) {
        Instant now = Instant.now();
        return JWT.create().withSubject(subject)
                .withIssuer(ISSUER)
                .withIssuedAt(Date.from(now))
                .withExpiresAt(Date.from(now.plus(mobileExpiration, ChronoUnit.MINUTES)))
                .withClaim(DEVICE_ID, deviceId)
                .sign(Algorithm.HMAC256(secret));
    }
}
