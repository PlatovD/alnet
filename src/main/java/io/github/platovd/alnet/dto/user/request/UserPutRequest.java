package io.github.platovd.alnet.dto.user.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(name = "Запрос изменения пользователя")
public class UserPutRequest {
    @Schema(name = "Имя пользователя")
    @Size(min = 1, max = 70)
    @NotBlank
    private String username;

    @Schema(name = "Электронная почта")
    @Size(min = 3, max = 70)
    @NotBlank
    private String email;
}
