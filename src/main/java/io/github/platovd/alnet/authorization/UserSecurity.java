package io.github.platovd.alnet.authorization;

import io.github.platovd.alnet.service.atomic.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component("userSecurity")
@RequiredArgsConstructor
public class UserSecurity {
    private final UserService userService;

    public boolean isCurrentUser(Long userId) {
        return userService.isCurrentUserById(userId);
    }
}
