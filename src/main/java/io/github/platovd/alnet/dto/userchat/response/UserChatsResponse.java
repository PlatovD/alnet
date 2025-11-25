package io.github.platovd.alnet.dto.userchat.response;

import io.github.platovd.alnet.mapper.info.ChatInfo;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Collection;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Ответ с информацией о чатах")
public class UserChatsResponse {
    @Schema(description = "Чат-лист подготовлен для пользователя с логином", example = "alexis125")
    private String login;

    @Schema(description = "Список чатов данного пользователя")
    private Collection<ChatInfo> chats;
}
