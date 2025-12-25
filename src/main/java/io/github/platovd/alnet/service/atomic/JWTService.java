package io.github.platovd.alnet.service.atomic;

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
 * Сервис для работы с jwt токенами (access + refresh).
 * Умеет выпускать их, проверять их и извлекать из них данные.
 *
 * @author PlatovD
 * @version 1.0
 */
@Service
@Setter
public class JWTService {

    /**
     * Секретный ключ для подписи JWT токенов.
     */
    @Value("${auth.jwt.singing.key}")
    private String key;

    /**
     * Время жизни access токена в секундах.
     */
    @Value("${auth.jwt.duration.expiration.access}")
    private Long accessTokenExpirationDuration;

    /**
     * Время жизни refresh токена в секундах.
     */
    @Value("${auth.jwt.duration.expiration.refresh}")
    private Long refreshTokenExpirationDuration;

    /**
     * Извлекает имя пользователя из JWT токена.
     *
     * @param token JWT токен
     * @return имя пользователя
     * @throws JwtException если токен невалиден
     */
    public String extractUserName(String token) throws JwtException {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * Извлекает идентификатор пользователя из JWT токена.
     *
     * @param token JWT токен
     * @return идентификатор пользователя
     * @throws JwtException если токен невалиден
     */
    public Long extractId(String token) throws JwtException {
        return Long.valueOf(extractClaim(token, Claims::getId));
    }

    /**
     * Извлекает тип токена из JWT токена.
     *
     * @param token JWT токен
     * @return тип токена (access или refresh)
     * @throws UnknownTokenTypeException если тип токена неизвестен
     */
    public String extractTokenType(String token) throws IllegalArgumentException {
        try {
            return extractAllClaims(token).get("type", String.class);
        } catch (IllegalArgumentException | JwtException e) {
            throw new UnknownTokenTypeException("Unknown token type exception. " + e);
        }
    }

    /**
     * Проверяет валидность JWT токена для указанного пользователя.
     *
     * @param token JWT токен для проверки
     * @param user пользователь для проверки
     * @return true если токен валиден для пользователя, иначе false
     */
    public boolean isTokenValid(String token, User user) {
        try {
            final String username = extractUserName(token);
            return (username.equals(user.getUsername()) && !isTokenExpired(token));
        } catch (JwtException e) {
            return false;
        }
    }

    /**
     * Генерирует access JWT токен для пользователя.
     *
     * @param user пользователь для которого генерируется токен
     * @return сгенерированный access токен
     */
    public String generateJWTAccess(User user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("type", "access");
        return generateJWTAccess(claims, user, accessTokenExpirationDuration);
    }

    /**
     * Генерирует refresh JWT токен для пользователя.
     *
     * @param user пользователь для которого генерируется токен
     * @return сгенерированный refresh токен
     */
    public String generateJWTRefresh(User user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("type", "refresh");
        return generateJWTAccess(claims, user, refreshTokenExpirationDuration);
    }

    /**
     * Проверяет, истек ли срок действия токена.
     *
     * @param token JWT токен для проверки
     * @return true если токен истек, иначе false
     */
    private boolean isTokenExpired(String token) {
        return getTokenExpiration(token).before(new Date(System.currentTimeMillis()));
    }

    /**
     * Получает дату истечения срока действия токена.
     *
     * @param token JWT токен
     * @return дата истечения срока действия
     */
    private Date getTokenExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    /**
     * Извлекает конкретное claim из токена.
     *
     * @param token JWT токен
     * @param claimsResolvers функция для извлечения конкретного claim
     * @return значение claim
     * @throws JwtException если токен невалиден
     */
    private <T> T extractClaim(String token, Function<Claims, T> claimsResolvers) throws JwtException {
        final Claims claims = extractAllClaims(token);
        return claimsResolvers.apply(claims);
    }

    /**
     * Извлекает все claims из токена.
     *
     * @param token JWT токен
     * @return все claims токена
     * @throws JwtException если токен невалиден
     */
    private Claims extractAllClaims(String token) throws JwtException {
        return Jwts.parser().verifyWith(getSigningKey(key)).build().parseSignedClaims(token).getPayload();
    }

    /**
     * Получает ключ для подписи токенов.
     *
     * @param key строка с ключом в Base64
     * @return секретный ключ
     */
    private SecretKey getSigningKey(String key) {
        byte[] keyBites = Decoders.BASE64.decode(key);
        return Keys.hmacShaKeyFor(keyBites);
    }

    /**
     * Генерирует JWT токен с указанными claims.
     *
     * @param claims дополнительные claims для токена
     * @param user пользователь для которого генерируется токен
     * @param expirationDurationSeconds время жизни токена в секундах
     * @return сгенерированный JWT токен
     */
    private String generateJWTAccess(Map<String, Object> claims, User user, Long expirationDurationSeconds) {
        return Jwts.builder()
                .header().add("typ", "JWT").and()
                .id(user.getUserId().toString()).subject(user.getUsername()).claims(claims)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + expirationToMillis(expirationDurationSeconds)))
                .signWith(getSigningKey(key)).compact();
    }

    /**
     * Конвертирует секунды в миллисекунды.
     *
     * @param expirationDurationSeconds время в секундах
     * @return время в миллисекундах
     */
    private Long expirationToMillis(Long expirationDurationSeconds) {
        return expirationDurationSeconds * 1000;
    }

    /**
     * Проверяет, соответствует ли тип токена указанному типу.
     *
     * @param token JWT токен
     * @param type ожидаемый тип токена
     * @return true если типы совпадают, иначе false
     */
    public boolean isTypeOf(String token, String type) {
        return extractTokenType(token).equals(type.strip().toLowerCase());
    }
}