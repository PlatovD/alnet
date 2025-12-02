package io.github.platovd.alnet.dto.userchat.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(name = "Запрос на операцию с пользователем чата")
public class UserChatOperationRequest {
    @Schema(name = "Идентификатор чата")
    private Long chatId;

    @Schema(name = "Имя пользователя")
    private List<String> usernames;
}
