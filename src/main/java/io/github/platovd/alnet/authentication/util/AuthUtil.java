package io.github.platovd.alnet.authentication.util;

import io.github.platovd.alnet.entity.Role;
import io.github.platovd.alnet.entity.User;
import org.springframework.security.core.userdetails.UserDetails;

public class AuthUtil {
    public static UserDetails fromUserToUserDetails(User user) {
        String[] roles = user.getRole().stream().map(Role::getName).toArray(String[]::new);
        return org.springframework.security.core.userdetails.User
                .builder()
                .username(user.getUsername())
                .password(user.getPassword())
                .roles(roles)
                .build();
    }
}
