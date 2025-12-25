package io.github.platovd.alnet.dto.message.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Сообщение для отображения")
public class MessageResponse {
    private Long id;
    private String content;
    private LocalDateTime dateTime;
    private String username;
    private Long chatId;
}
