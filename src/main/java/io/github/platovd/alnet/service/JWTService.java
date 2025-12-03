package io.github.platovd.alnet.service;

import io.github.platovd.alnet.entity.User;
import io.github.platovd.alnet.exception.authentication.UnknownTokenTypeException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Сервис для работы с jwt токенами (access + refresh). Умеет выпускать их, проверять их и извлекать из них данные.
 */
@Service
@Setter
public class JWTService {
    @Value("${auth.jwt.singing.key}")
    private String key;

    @Value("${auth.jwt.duration.expiration.access}")
    private Long accessTokenExpirationDuration;

    @Value("${auth.jwt.duration.expiration.refresh}")
    private Long refreshTokenExpirationDuration;

    public String extractUserName(String token) throws JwtException {
        return extractClaim(token, Claims::getSubject);
    }

    public Long extractId(String token) throws JwtException {
        return Long.valueOf(extractClaim(token, Claims::getId));
    }

    public String extractTokenType(String token) throws IllegalArgumentException {
        try {
            return extractAllClaims(token).get("type", String.class);
        } catch (IllegalArgumentException | JwtException e) {
            throw new UnknownTokenTypeException("Unknown token type exception. " + e);
        }
    }

    public boolean isTokenValid(String token, User user) {
        try {
            final String username = extractUserName(token);
            return (username.equals(user.getUsername()) && !isTokenExpired(token));
        } catch (JwtException e) {
            return false;
        }
    }

    public String generateJWTAccess(User user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("type", "access");
        return generateJWTAccess(claims, user, accessTokenExpirationDuration);
    }

    public String generateJWTRefresh(User user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("type", "refresh");
        return generateJWTAccess(claims, user, refreshTokenExpirationDuration);
    }

    private boolean isTokenExpired(String token) {
        return getTokenExpiration(token).before(new Date(System.currentTimeMillis()));
    }

    private Date getTokenExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    private <T> T extractClaim(String token, Function<Claims, T> claimsResolvers) throws JwtException {
        final Claims claims = extractAllClaims(token);
        return claimsResolvers.apply(claims);
    }

    private Claims extractAllClaims(String token) throws JwtException {
        return Jwts.parser().verifyWith(getSigningKey(key)).build().parseSignedClaims(token).getPayload();
    }

    private SecretKey getSigningKey(String key) {
        byte[] keyBites = Decoders.BASE64.decode(key);
        return Keys.hmacShaKeyFor(keyBites);
    }

    private String generateJWTAccess(Map<String, Object> claims, User user, Long expirationDurationSeconds) {
        return Jwts.builder()
                .header().add("typ", "JWT").and()
                .id(user.getUserId().toString()).subject(user.getUsername()).claims(claims)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + expirationToMillis(expirationDurationSeconds)))
                .signWith(getSigningKey(key)).compact();
    }

    private Long expirationToMillis(Long expirationDurationSeconds) {
        return expirationDurationSeconds * 1000;
    }

    public boolean isTypeOf(String token, String type) {
        return extractTokenType(token).equals(type.strip().toLowerCase());
    }
}
