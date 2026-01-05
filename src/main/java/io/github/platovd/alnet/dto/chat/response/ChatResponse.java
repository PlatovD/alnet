package io.github.platovd.alnet.dto.chat.response;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;

@Schema(description = "Ответ в виде сущности чата")
@AllArgsConstructor
@Data
@NotNull
public class ChatResponse {
    private Long id;
    private String name;
}
