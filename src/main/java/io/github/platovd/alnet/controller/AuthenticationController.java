package io.github.platovd.alnet.controller;

import io.github.platovd.alnet.dto.authentication.response.JWTAuthenticationResponse;
import io.github.platovd.alnet.dto.authentication.request.RefreshRequest;
import io.github.platovd.alnet.dto.authentication.request.SignInRequest;
import io.github.platovd.alnet.dto.authentication.request.SignUpRequest;
import io.github.platovd.alnet.service.orchestration.UserFacade;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * Контроллер для обработки запросов аутентификации и авторизации.
 * Предоставляет endpoints для регистрации, входа и обновления токенов.
 *
 * @author PlatovD
 * @version 1.0
 */
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Аутентификация")
public class AuthenticationController {

    /**
     * Фасад для работы с пользователями.
     */
    private final UserFacade userFacade;

    /**
     * Регистрирует нового пользователя в системе.
     *
     * @param request данные для регистрации пользователя
     * @return объект с JWT токенами доступа и обновления
     */
    @Operation(summary = "Регистрация пользователя")
    @PostMapping("/sign-up")
    public JWTAuthenticationResponse signUp(@RequestBody @Valid SignUpRequest request) {
        return userFacade.signUp(request);
    }

    /**
     * Аутентифицирует пользователя в системе.
     *
     * @param request данные для аутентификации пользователя
     * @return объект с JWT токенами доступа и обновления
     */
    @Operation(summary = "Аутентификация пользователя")
    @PostMapping("/sign-in")
    public JWTAuthenticationResponse signIn(@RequestBody @Valid SignInRequest request) {
        return userFacade.signIn(request);
    }

    /**
     * Обновляет JWT токены доступа с использованием токена обновления.
     *
     * @param request запрос с токеном обновления
     * @return объект с новыми JWT токенами доступа и обновления
     */
    @Operation(summary = "Обновление токенов доступа")
    @PostMapping("/refresh")
    private JWTAuthenticationResponse refresh(@RequestBody @Valid RefreshRequest request) {
        return userFacade.refresh(request);
    }
}