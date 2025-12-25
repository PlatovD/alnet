package io.github.platovd.alnet.authentication.util;

import io.github.platovd.alnet.entity.Role;
import io.github.platovd.alnet.entity.User;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * Утилитарный класс для работы с аутентификацией.
 * Содержит методы преобразования объектов между слоями приложения.
 */
public class AuthUtil {

    /**
     * Преобразует объект User в объект UserDetails для использования в Spring Security.
     *
     * @param user объект пользователя из базы данных
     * @return объект UserDetails с информацией о пользователе и его ролях
     */
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