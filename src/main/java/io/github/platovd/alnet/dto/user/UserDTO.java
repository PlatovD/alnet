package io.github.platovd.alnet.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Schema(name = "Ответ в виде сущности пользователя")
@Builder
@AllArgsConstructor
@NoArgsConstructor
@NotNull
public class UserDTO {
    @Min(1)
    @Max(Long.MAX_VALUE)
    private Long userId;
    @Size(min = 3)
    private String username;
    @Email
    private String email;
}
