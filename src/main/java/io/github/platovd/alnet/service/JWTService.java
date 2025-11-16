package io.github.platovd.alnet.service;

import io.github.platovd.alnet.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
public class JWTService {
    @Value("${jwt.singing.key}")
    private String key;

    @Value("${jwt.duration.expiration.access}")
    private Long accessTokenExpirationDuration;

    @Value("${jwt.duration.expiration.refresh}")
    private Long refreshTokenExpirationDuration;

    public String extractUserName(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public String extractTokenType(String token) {
        try {
            return extractAllClaims(token).get("type", String.class);
        } catch (IllegalArgumentException | JwtException e) {
            throw new IllegalArgumentException("Unknown token type error. " + e);
        }
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUserName(token);
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }

    public String generateJWT(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        if (userDetails instanceof User customUserDetails) {
            claims.put("type", "access");
            claims.put("id", customUserDetails.getId());
            claims.put("email", customUserDetails.getEmail());
            claims.put("role", customUserDetails.getRole());
        }
        return generateJWT(claims, userDetails, accessTokenExpirationDuration);
    }

    public String generateJWTRefresh(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("type", "refresh");
        return generateJWT(claims, userDetails, refreshTokenExpirationDuration);
    }

    private boolean isTokenExpired(String token) {
        return getTokenExpiration(token).before(new Date(System.currentTimeMillis()));
    }

    private Date getTokenExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    private <T> T extractClaim(String token, Function<Claims, T> claimsResolvers) {
        final Claims claims = extractAllClaims(token);
        return claimsResolvers.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser().verifyWith(getSigningKey(key)).build().parseSignedClaims(token).getPayload();
    }

    private SecretKey getSigningKey(String key) {
        byte[] keyBites = Decoders.BASE64.decode(key);
        return Keys.hmacShaKeyFor(keyBites);
    }

    private String generateJWT(Map<String, Object> claims, UserDetails userDetails, Long expirationDurationSeconds) {
        return Jwts.builder()
                .header().add("typ", "JWT").and()
                .claims(claims).subject(userDetails.getUsername())
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + 1000 * expirationDurationSeconds))
                .signWith(getSigningKey(key)).compact();
    }
}
