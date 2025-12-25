package io.github.platovd.alnet.authentication.provider;

import io.github.platovd.alnet.authentication.token.JWTAuthToken;
import io.github.platovd.alnet.authentication.util.AuthUtil;
import io.github.platovd.alnet.entity.User;
import io.github.platovd.alnet.exception.authentication.IllegalTokenClassException;
import io.github.platovd.alnet.exception.authentication.InvalidAccessTokenException;
import io.github.platovd.alnet.exception.authentication.UnknownAuthenticationException;
import io.github.platovd.alnet.repository.UserRepository;
import io.github.platovd.alnet.service.atomic.JWTService;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Кастомный класс аутентификации, который добавляется в ProviderManager, который в свою очередь имплементит
 * Authentication Manager. При создании Authentication Manager в Security Config этот компонент добавляется в
 * коллекцию Authentication провайдеров, и для тех объектов Authentication, для которых этот провайдер подойдет,
 * он будет вызван. Если этот провайдер не сможет произвести аутентификацию - он выбросит AuthenticationException,
 * иначе - вернет объект Authentication, который будет уже иметь isAuthenticated() = true.
 *
 * @author PlatovD
 * @version 1.0
 */
@Component
@RequiredArgsConstructor
public class JWTAuthenticationProvider implements AuthenticationProvider {

    /**
     * Сервис для работы с JWT токенами.
     */
    private final JWTService jwtService;

    /**
     * Репозиторий для доступа к данным пользователей.
     */
    private final UserRepository userRepository;

    /**
     * Требуемый тип токена для аутентификации.
     */
    private final String REQUIRED_TOKEN_TYPE = "access";

    /**
     * Аутентифицирует пользователя на основе JWT токена.
     * Проверяет валидность токена, извлекает информацию о пользователе и создает аутентифицированный объект.
     *
     * @param authentication объект аутентификации для обработки
     * @return аутентифицированный объект JWTAuthToken
     * @throws AuthenticationException если аутентификация не удалась
     */
    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        // смотрим, передали ли нам поддерживаемую Authentication
        if (!this.supports(authentication.getClass()))
            throw new IllegalTokenClassException(
                    "JWTAuthenticationProvider only supports JWTAuthToken, but got: " +
                            authentication.getClass().getName()
            );
        // кастим
        JWTAuthToken jwtAuth = (JWTAuthToken) authentication;
        // пробуем вытащить claims из токена, проверить наличие пользователя, проверить валидность токена
        try {
            String token = jwtAuth.getToken();
            Long userId = jwtService.extractId(token); // выбросит JwtException, если токен не валиден по ключу шифрования
            Optional<User> user = userRepository.findById(userId); // здесь использую репо специально чтобы не было циклической зависимости в userservice
            if (user.isEmpty()) throw new JwtException("No user with extracted id");

            // проверяю токен на истечение, проверяю тип токена
            if (!jwtService.isTypeOf(token, REQUIRED_TOKEN_TYPE) || !jwtService.isTokenValid(token, user.orElse(null)))
                throw new InvalidAccessTokenException("Given token isn't valid for user");

            // маппим User -> UserDetails для сохранения информации о пользователе в объекте Authentication
            // делаю это для того, чтобы отделить бизнес логику и обертку для Security
            UserDetails details = AuthUtil.fromUserToUserDetails(user.orElse(null));
            // возвращаю Authentication с isAuthenticated() = true
            return new JWTAuthToken(
                    token,
                    details,
                    details.getAuthorities(),
                    true,
                    userId
            );

        } catch (JwtException exception) {
            throw new InvalidAccessTokenException("Authentication went wrong. Access denied. Cause: " + exception.getMessage());
        } catch (Exception e) {
            throw new UnknownAuthenticationException("Authentication failed. Unknown exception");
        }
    }

    /**
     * Проверяет, поддерживает ли данный провайдер указанный класс аутентификации.
     *
     * @param authentication класс для проверки поддержки
     * @return true если провайдер поддерживает JWTAuthToken, иначе false
     * @throws AuthenticationException если возникает ошибка при проверке поддержки
     */
    @Override
    public boolean supports(Class<?> authentication) throws AuthenticationException {
        return JWTAuthToken.class.isAssignableFrom(authentication);
    }
}