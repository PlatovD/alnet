package io.github.platovd.alnet.dto.authentication.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Schema(description = "Запрос на аутентификацию")
public class SignInRequest {
    @Schema(description = "Имя пользователя", example = "Dima")
    @Size(min = 3, max = 50, message = "Имя пользователя должно содержать от 3 до 50 символов")
    @NotBlank(message = "Име пользователя не может быть пустым")
    private String name;

    @Schema(description = "Пароль", example = "my_1ecer10_pa66")
    @Size(min = 6, message = "Пароль должен содержать как минимум 6 символов")
    @NotBlank(message = "Пароль не должен быть пустым")
    private String password;
}
