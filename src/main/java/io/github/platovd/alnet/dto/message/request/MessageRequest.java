package io.github.platovd.alnet.dto.message.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(description = "Сообщение для сохранения. Чат будет браться из url, а пользователь будет взят из текущей аутентификации")
public class MessageRequest {
    @NotBlank
    @Size(min = 1, max = 5000)
    private String content;
}
