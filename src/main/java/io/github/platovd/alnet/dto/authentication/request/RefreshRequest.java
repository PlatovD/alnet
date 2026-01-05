package io.github.platovd.alnet.dto.authentication.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Schema(description = "Запрос на обновление JWT")
@NotNull
public class RefreshRequest {
    @Size(max = 150)
    private String refresh;
}
