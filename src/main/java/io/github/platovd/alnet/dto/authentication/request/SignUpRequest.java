package io.github.platovd.alnet.dto.authentication.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Schema(description = "Запрос на регистрацию")
public class SignUpRequest {
    @Schema(description = "Имя пользователя", example = "Dima")
    @Size(min = 3, max = 50, message = "Имя пользователя должно содержать от 3 до 50 символов")
    @NotBlank(message = "Име пользователя не может быть пустым")
    private String name;

    @Schema(description = "Email", example = "dima@gmail.com")
    @Size(min = 5, max = 100, message = "Email должен содержать от 5 до 100 символов")
    @NotBlank(message = "Email должен быть не пустым")
    @Email(message = "Email должен быть записан в правильном формате")
    private String email;

    @Schema(description = "Пароль", example = "my_1ecer10_pa66")
    @Size(min = 6, message = "Пароль должен содержать как минимум 6 символов")
    @NotBlank(message = "Пароль не должен быть пустым")
    private String password;
}
