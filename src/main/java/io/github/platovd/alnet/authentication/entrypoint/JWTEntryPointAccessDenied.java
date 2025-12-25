package io.github.platovd.alnet.authentication.entrypoint;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;


/**
 * Класс, который отвечает за обработку ошибок авторизации.
 * Если пользователь не имеет достаточно прав для просмотра определенного контента,
 * то этот класс обработает эту ошибку и сформирует соответствующий ответ клиентской части.
 *
 * @author PlatovD
 * @version 1.0
 */
@Component
@RequiredArgsConstructor
public class JWTEntryPointAccessDenied implements AccessDeniedHandler {

    /**
     * Объект для преобразования Java-объектов в JSON и обратно.
     * Используется для формирования JSON-ответа с ошибкой.
     */
    private final ObjectMapper objectMapper;

    /**
     * Обрабатывает ситуацию, когда доступ к ресурсу запрещен из-за недостаточных прав.
     * Формирует JSON-ответ с HTTP статусом 403 (Forbidden).
     *
     * @param request HTTP-запрос, который привел к ошибке доступа
     * @param response HTTP-ответ, в который будет записан результат обработки
     * @param accessDeniedException исключение, содержащее информацию об ошибке доступа
     * @throws IOException если возникает ошибка ввода-вывода при работе с ответом
     * @throws ServletException если возникает ошибка сервлета
     */
    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) throws IOException, ServletException {
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        Map<String, String> errorResponse = Map.of(
                "error", "Unauthorized",
                "message", accessDeniedException.getMessage()
        );
        String error = objectMapper.writeValueAsString(errorResponse);
        response.getWriter().print(error);
    }
}