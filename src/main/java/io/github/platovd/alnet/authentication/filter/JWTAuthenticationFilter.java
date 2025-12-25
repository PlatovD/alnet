package io.github.platovd.alnet.authentication.filter;

import io.github.platovd.alnet.authentication.contex.SecurityContextWrapper;
import io.github.platovd.alnet.authentication.token.JWTAuthToken;
import io.github.platovd.alnet.exception.authentication.InvalidAccessTokenException;
import io.micrometer.common.lang.NonNull;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Кастомный фильтр для JWT аутентификации. Использует Authentication Manager, чтобы попробовать аутентифицировать
 * текущего пользователя. По факту ядро JWT аутентификации. Этот фильтр добавлен в стандартную SecurityChain,
 * предоставляемую Spring(ом), перед стандартным BasicAuthenticationFilter. Если запрос содержит аутентификацию
 * через JWT, то фильтр попробует ее произвести, иначе продолжит цепочку фильтров. Так, например, при
 * входе через username + password, будет использован именно BasicAuthenticationFilter
 *
 * @author PlatovD
 * @version 1.0
 */
@Builder
@RequiredArgsConstructor
public class JWTAuthenticationFilter extends OncePerRequestFilter {

    /**
     * Префикс для Bearer токена в заголовке Authorization.
     */
    public static final String BEARER_PREFIX = "Bearer ";

    /**
     * Имя HTTP-заголовка, содержащего токен аутентификации.
     */
    public static final String HEADER_NAME = "Authorization";

    /**
     * Обертка для работы с контекстом безопасности.
     */
    private final SecurityContextWrapper securityContextWrapper;

    /**
     * Менеджер аутентификации для проверки JWT токенов.
     */
    private final AuthenticationManager authManager;

    /**
     * Точка входа для обработки ошибок аутентификации.
     */
    private final AuthenticationEntryPoint authenticationEntryPoint;

    /**
     * Флаг, указывающий игнорировать ли ошибки аутентификации и продолжать цепочку фильтров.
     */
    private final boolean ignoreFailure = false;

    /**
     * Основной метод фильтрации запросов.
     * Обрабатывает JWT аутентификацию для каждого HTTP-запроса.
     *
     * @param request HTTP-запрос
     * @param response HTTP-ответ
     * @param filterChain цепочка фильтров
     * @throws ServletException если возникает ошибка сервлета
     * @throws IOException если возникает ошибка ввода-вывода
     */
    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain filterChain) throws ServletException, IOException {
        // оборачиваю, чтобы поймать ошибки аутентификации
        try {
            // проверяю, есть ли заголовок "Authorization: Bearer <token>"
            if (!checkBearer(request) || isAuthenticated()) {
                doFilter(request, response, filterChain);
                return;
            }
            // извлекаю токен из заголовка
            var jwt = request.getHeader(HEADER_NAME).substring(BEARER_PREFIX.length());
            // если токен пуст
            if (jwt.isEmpty()) {
                throw new InvalidAccessTokenException("JWT token can't be empty");
            }

            // Если все выглядит валидным и аутентификация требуется (нет подтвержденной). Если все пройдет хорошо,
            // то данный код создаст подтвержденную аутентификацию и положит ее в SecurityContext, иначе - выбросит
            // AuthenticationException
            Authentication token = new JWTAuthToken(jwt);
            Authentication authentication = authManager.authenticate(token);
            securityContextWrapper.setAuthentication(authentication);
            onSuccessfulAuthentication(request, response, authentication);
        } catch (AuthenticationException e) {
            securityContextWrapper.unAuthenticate();
            onUnsuccessfulAuthentication(request, response, e);
            if (ignoreFailure) {
                doFilter(request, response, filterChain);
            } else {
                authenticationEntryPoint.commence(request, response, e);
            }
            return;
        }
        doFilter(request, response, filterChain);
    }

    /**
     * Проверяет, аутентифицирован ли текущий пользователь.
     *
     * @return true если пользователь аутентифицирован, иначе false
     */
    protected boolean isAuthenticated() {
        return securityContextWrapper.isAuthenticated();
    }

    /**
     * Проверяет наличие Bearer токена в заголовке Authorization запроса.
     *
     * @param request HTTP-запрос для проверки
     * @return true если заголовок Authorization содержит Bearer токен, иначе false
     */
    protected boolean checkBearer(@NonNull HttpServletRequest request) {
        String header = request.getHeader(HEADER_NAME);
        return !StringUtils.isEmpty(header) && header.startsWith(BEARER_PREFIX);
    }

    /**
     * Вызывается при успешной аутентификации.
     * Может быть переопределен для добавления дополнительной логики.
     *
     * @param request HTTP-запрос
     * @param response HTTP-ответ
     * @param authResult результат успешной аутентификации
     * @throws IOException если возникает ошибка ввода-вывода
     */
    protected void onSuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response, Authentication
            authResult) throws IOException {
    }

    /**
     * Вызывается при неудачной аутентификации.
     * Может быть переопределен для добавления дополнительной логики.
     *
     * @param request HTTP-запрос
     * @param response HTTP-ответ
     * @param failed исключение аутентификации
     * @throws IOException если возникает ошибка ввода-вывода
     */
    protected void onUnsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response, AuthenticationException
            failed) throws IOException {
    }
}