package io.github.platovd.alnet.dto.userchat.response;

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
@Schema(name = "Список пользователей данного чата")
public class ChatMembersResponse {
    @Schema(name = "Идентификатор чата")
    private Long chatId;

    @Schema(name = "Участники чата")
    private List<ChatMemberDTO> members;
}
