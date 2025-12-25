package io.github.platovd.alnet.authentication.token;

import lombok.AllArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;

/**
 * Класс, представляющий JWT токен аутентификации.
 * Реализует интерфейс Authentication для интеграции с Spring Security.
 *
 * @author PlatovD
 * @version 1.0
 */
@AllArgsConstructor
public class JWTAuthToken implements Authentication {

    /**
     * JWT токен в виде строки.
     */
    private final String jwtToken;

    /**
     * Информация о пользователе.
     */
    private UserDetails principal;

    /**
     * Коллекция прав доступа пользователя.
     */
    private Collection<? extends GrantedAuthority> authorities;

    /**
     * Флаг, указывающий аутентифицирован ли пользователь.
     */
    private boolean isAuthenticated = false;

    /**
     * Идентификатор пользователя.
     */
    private Long principalId;

    /**
     * Конструктор для создания неаутентифицированного токена.
     *
     * @param jwtToken JWT токен
     */
    public JWTAuthToken(String jwtToken) {
        this.jwtToken = jwtToken;
    }

    /**
     * Возвращает права доступа пользователя.
     *
     * @return коллекция прав доступа
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    /**
     * Возвращает учетные данные аутентификации (JWT токен).
     *
     * @return JWT токен
     */
    @Override
    public Object getCredentials() {
        return jwtToken;
    }

    /**
     * Возвращает дополнительную информацию об аутентификации.
     *
     * @return null, так как дополнительная информация не используется
     */
    @Override
    public Object getDetails() {
        return null;
    }

    /**
     * Возвращает информацию о пользователе.
     *
     * @return объект UserDetails
     */
    @Override
    public Object getPrincipal() {
        return principal;
    }

    /**
     * Проверяет, аутентифицирован ли пользователь.
     *
     * @return true если пользователь аутентифицирован, иначе false
     */
    @Override
    public boolean isAuthenticated() {
        return isAuthenticated;
    }

    /**
     * Устанавливает флаг аутентификации.
     *
     * @param isAuthenticated значение флага аутентификации
     * @throws IllegalArgumentException если передано некорректное значение
     */
    @Override
    public void setAuthenticated(boolean isAuthenticated) throws IllegalArgumentException {
        this.isAuthenticated = isAuthenticated;
    }

    /**
     * Возвращает имя пользователя.
     *
     * @return имя пользователя или пустая строка если principal отсутствует
     */
    @Override
    public String getName() {
        if (principal == null) return "";
        return principal.getUsername();
    }

    /**
     * Возвращает идентификатор пользователя.
     *
     * @return идентификатор пользователя
     */
    public Long getId() {
        return principalId;
    }

    /**
     * Возвращает JWT токен.
     *
     * @return JWT токен
     */
    public String getToken() {
        return jwtToken;
    }
}