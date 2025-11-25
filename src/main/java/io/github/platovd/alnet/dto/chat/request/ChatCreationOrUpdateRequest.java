package io.github.platovd.alnet.dto.chat.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
@Schema(name = "Запрос на создание чата")
public class ChatCreationOrUpdateRequest {
    @Schema(name = "Идентификатор чата, используется при PUT запросе", example = "456")
    @Max(value = Long.MAX_VALUE)
    private Long chatId;

    @Schema(name = "Название чата", example = "Мои друзья")
    @Size(max = 30)
    private String name;

    @Schema(name = "Пользователи")
    @Size(max = 100)
    private List<String> members;
}
