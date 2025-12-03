package io.github.platovd.alnet.dto.user.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Data
@Schema(name = "Ответ в виде сущности пользователя")
@Builder
public class UserDTO {
    @Schema(name = "Имя пользователя")
    private String username;
    @Schema(name = "Электронная почта пользователя", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String email;
}
