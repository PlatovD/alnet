package io.github.platovd.alnet.dto.chat.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

@Schema(name = "Ответ в виде сущности чата")
@AllArgsConstructor
@Data
public class ChatDTO {
    @Schema(name = "Идентификатор чата")
    private Long id;
    @Schema(name = "Название чата")
    private String name;
}
