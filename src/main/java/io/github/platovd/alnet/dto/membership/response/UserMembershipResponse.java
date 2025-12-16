package io.github.platovd.alnet.dto.membership.response;

import io.github.platovd.alnet.entity.User;
import lombok.Data;

@Data
public class UserMembershipResponse {
    private final String username;

    public UserMembershipResponse(User user) {
        this.username = user.getUsername();
    }
}