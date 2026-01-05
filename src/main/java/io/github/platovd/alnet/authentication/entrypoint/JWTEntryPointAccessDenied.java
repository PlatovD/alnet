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
 * Класс, который отвечает за обработку ошибок авторизации. Если пользователь не имеет достаточно прав для просмотра
 * определенного контента,то этот класс обработает эту ошибку и сформирует соответсвующий ответ клиентской части
 */
@Component
@RequiredArgsConstructor
public class JWTEntryPointAccessDenied implements AccessDeniedHandler {
    private final ObjectMapper objectMapper;

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
