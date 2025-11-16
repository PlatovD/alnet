package io.github.platovd.alnet.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Ответ с токеном доступа")
public class JWTAuthenticationResponse {
    @Schema(description = "Токен доступа")
    private String token;

    @Schema(description = "Токен обновления")
    private String refresh;
}
