package io.github.platovd.alnet.authentication.contex;

import io.github.platovd.alnet.exception.authentication.NoAuthenticationCredentialsException;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.function.Function;

/**
 * Класс, реализующий обертку над SecurityContextHolder.
 * Предоставляет удобный интерфейс для работы с контекстом безопасности Spring Security
 * и позволяет выполнять операции над контекстом с помощью отдельных функций.
 *
 * @author PlatovD
 * @version 1.0
 */
@Component
@RequiredArgsConstructor
@Setter
public class SecurityContextWrapper {

    /**
     * Ключ для анонимной аутентификации.
     * Значение загружается из конфигурации приложения по ключу "auth.anonymous.key".
     */
    @Value("${auth.anonymous.key}")
    private String anonymousAuthKey;

    /**
     * Получает текущий контекст безопасности.
     *
     * @return текущий контекст безопасности
     * @see SecurityContextHolder#getContext()
     */
    public SecurityContext getContext() {
        return SecurityContextHolder.getContext();
    }

    /**
     * Проверяет, аутентифицирован ли текущий пользователь.
     * Возвращает false если аутентификация отсутствует, является анонимной или не подтверждена.
     *
     * @return true если пользователь аутентифицирован, иначе false
     */
    public boolean isAuthenticated() {
        SecurityContext context = SecurityContextHolder.getContext();
        Authentication authentication = context.getAuthentication();

        return authentication != null
                && !(authentication instanceof AnonymousAuthenticationToken)
                && authentication.isAuthenticated();
    }

    /**
     * Получает учетные данные аутентификации текущего пользователя.
     *
     * @return учетные данные аутентификации
     * @throws NoAuthenticationCredentialsException если пользователь не аутентифицирован
     */
    public Object getAuthenticationCredentials() {
        SecurityContext context = SecurityContextHolder.getContext();
        Authentication authentication = context.getAuthentication();
        if (!isAuthenticated())
            throw new NoAuthenticationCredentialsException("User authentication is not strong or not exists");
        return authentication.getCredentials();
    }

    /**
     * Получает информацию об аутентифицированном пользователе и преобразует ее с помощью переданной функции.
     *
     * @param <T> тип возвращаемого значения
     * @param resolver функция для преобразования UserDetails в нужный тип
     * @return результат применения функции к информации о пользователе
     * @throws NoAuthenticationCredentialsException если пользователь не аутентифицирован
     * @throws NoAuthenticationCredentialsException если principal имеет неподдерживаемый тип
     */
    public <T> T getAuthenticatedUserInfo(Function<UserDetails, T> resolver) {
        if (!isAuthenticated())
            throw new NoAuthenticationCredentialsException("User authentication is not strong or not exists");

        Object principal = getAuthentication().getPrincipal();
        if (!(principal instanceof UserDetails details))
            throw new NoAuthenticationCredentialsException("Principal is unsupported type " + principal.getClass().getName());

        return resolver.apply(details);
    }

    /**
     * Сбрасывает аутентификацию пользователя, устанавливая анонимную аутентификацию.
     * В случае ошибки при создании анонимного токена устанавливает ключ "Error key".
     */
    public void unAuthenticate() {
        SecurityContext context = SecurityContextHolder.getContext();
        try {
            context.setAuthentication(
                    new AnonymousAuthenticationToken(
                            anonymousAuthKey,
                            "anonymousUser",
                            List.of(new SimpleGrantedAuthority("ROLE_ANONYMOUS")))
            );
        } catch (IllegalArgumentException e) {
            anonymousAuthKey = "Error key";
        }
    }

    /**
     * Получает объект аутентификации из текущего контекста безопасности.
     *
     * @return объект аутентификации
     */
    public Authentication getAuthentication() {
        return getContext().getAuthentication();
    }

    /**
     * Устанавливает новую аутентификацию в контекст безопасности.
     * Перед установкой новой аутентификации сбрасывает текущую с помощью unAuthenticate().
     *
     * @param authentication объект аутентификации для установки
     */
    public void setAuthentication(Authentication authentication) {
        unAuthenticate();
        getContext().setAuthentication(authentication);
    }
}