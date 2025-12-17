package io.github.platovd.alnet.dto.membership.response;

import io.github.platovd.alnet.dto.chat.ChatDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
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
public class UserChatsMembershipResponse {
    @NotBlank
    @Size(min = 3)
    private String username;

    private Collection<ChatDTO> chats;
}
