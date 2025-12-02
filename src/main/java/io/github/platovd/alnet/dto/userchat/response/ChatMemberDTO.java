package io.github.platovd.alnet.dto.userchat.response;

import io.github.platovd.alnet.entity.User;
import lombok.Data;

@Data
public class ChatMemberDTO {
    private final String username;

    public ChatMemberDTO(User user) {
        this.username = user.getUsername();
    }
}
