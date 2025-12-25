package io.github.platovd.alnet.dto.membership.response;

import io.github.platovd.alnet.entity.User;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
@NotNull
public class UserMembershipResponse {
    @Size(min = 3)
    private final String username;

    public UserMembershipResponse(User user) {
        this.username = user.getUsername();
    }
}