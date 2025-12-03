package io.github.platovd.alnet.dto.user.response;

import io.github.platovd.alnet.entity.User;
import lombok.Data;

@Data
public class UserDTO {
    private final String username;

    public UserDTO(User user) {
        this.username = user.getUsername();
    }
}
