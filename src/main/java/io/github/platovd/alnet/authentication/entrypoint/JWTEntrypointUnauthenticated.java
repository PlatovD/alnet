package io.github.platovd.alnet.authentication.entrypoint;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;

/**
 * Класс, который отвечает за обработку ошибок JWT аутентификации.
 * Если процесс аутентификации использовал JWT и прошел неудачно,
 * то этот класс обрабатывает ошибку и формирует соответствующий ответ сервера.
 *
 * @author PlatovD
 * @version 1.0
 */
@Component
@RequiredArgsConstructor
public class JWTEntrypointUnauthenticated implements AuthenticationEntryPoint {

    /**
     * Объект для преобразования Java-объектов в JSON и обратно.
     * Используется для формирования JSON-ответа с ошибкой аутентификации.
     */
    private final ObjectMapper objectMapper;

    /**
     * Обрабатывает ситуацию, когда аутентификация не удалась или отсутствует.
     * Формирует JSON-ответ с HTTP статусом 401 (Unauthorized).
     *
     * @param request HTTP-запрос, который привел к ошибке аутентификации
     * @param response HTTP-ответ, в который будет записан результат обработки
     * @param authException исключение, содержащее информацию об ошибке аутентификации
     * @throws IOException если возникает ошибка ввода-вывода при работе с ответом
     * @throws ServletException если возникает ошибка сервлета
     */
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException, ServletException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        Map<String, String> errorResponse = Map.of(
                "error", "Unauthorized",
                "message", authException.getMessage()
        );
        String error = objectMapper.writeValueAsString(errorResponse);
        response.getWriter().print(error);
    }
}