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

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Аутентификация")
public class AuthenticationController {
    private final UserFacade userFacade;

    @Operation(summary = "Регистрация пользователя")
    @PostMapping("/sign-up")
    public JWTAuthenticationResponse signUp(@RequestBody @Valid SignUpRequest request) {
        return userFacade.signUp(request);
    }

    @Operation(summary = "Аутентификация пользователя")
    @PostMapping("/sign-in")
    public JWTAuthenticationResponse signIn(@RequestBody @Valid SignInRequest request) {
        return userFacade.signIn(request);
    }

    @Operation(summary = "Обновление токенов доступа")
    @PostMapping("/refresh")
    private JWTAuthenticationResponse refresh(@RequestBody @Valid RefreshRequest request) {
        return userFacade.refresh(request);
    }
}
