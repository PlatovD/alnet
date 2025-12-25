package io.github.platovd.alnet.dto.authentication.response;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Ответ с токеном доступа")
@NotNull
public class JWTAuthenticationResponse {
    private String token;

    private String refresh;
}
