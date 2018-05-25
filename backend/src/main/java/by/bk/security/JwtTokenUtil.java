package by.bk.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
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
    @Value("${jwt.secret}")
    private String secret;
    @Value("${jwt.expiration}")
    private long expiration;

    public Claims getAllClaimsFromToken(String token) throws ExpiredJwtException {
        return Jwts.parser()
                .setSigningKey(secret)
                .parseClaimsJws(token)
                .getBody();
    }

    public String generateToken(String subject) {
        Instant now = Instant.now();
        return Jwts.builder()
                .setSubject(subject)
                .setIssuer("https://deplake.tk")
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(now.plus(expiration, ChronoUnit.MINUTES)))
                .signWith(SignatureAlgorithm.HS256, secret)
                .compact();
    }
}