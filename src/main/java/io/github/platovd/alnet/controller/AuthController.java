package io.github.platovd.alnet.controller;

import io.github.platovd.alnet.dto.response.JWTAuthenticationResponse;
import io.github.platovd.alnet.dto.request.RefreshRequest;
import io.github.platovd.alnet.dto.request.SignInRequest;
import io.github.platovd.alnet.dto.request.SignUpRequest;
import io.github.platovd.alnet.service.AuthenticationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Аутентификация")
public class AuthController {
    private final AuthenticationService authenticationService;

    @Operation(summary = "Регистрация пользователя")
    @PostMapping("/sign-up")
    public JWTAuthenticationResponse signUp(@RequestBody @Valid SignUpRequest request) {
        return authenticationService.signUp(request);
    }

    @Operation(summary = "Аутентификация пользователя")
    @PostMapping("/sign-in")
    public JWTAuthenticationResponse signIn(@RequestBody @Valid SignInRequest request) {
        return authenticationService.signIn(request);
    }

    @Operation(summary = "Обновление токенов доступа")
    @PostMapping("/refresh")
    private JWTAuthenticationResponse refresh(@RequestBody @Valid RefreshRequest request) {
        return authenticationService.refresh(request);
    }
}
