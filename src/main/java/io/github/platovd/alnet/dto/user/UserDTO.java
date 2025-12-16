package io.github.platovd.alnet.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
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
    @Size(min = 3)
    private String username;
    @Email
    private String email;
}
