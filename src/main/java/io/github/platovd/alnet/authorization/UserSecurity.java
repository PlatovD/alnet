package io.github.platovd.alnet.authorization;

import io.github.platovd.alnet.service.atomic.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Компонент безопасности для проверки совпадения текущего пользователя.
 * Используется в аннотациях Spring Security для контроля доступа к ресурсам пользователей.
 *
 * @author PlatovD
 * @version 1.0
 */
@Component("userSecurity")
@RequiredArgsConstructor
public class UserSecurity {

    /**
     * Сервис для работы с пользователями.
     */
    private final UserService userService;

    /**
     * Проверяет, соответствует ли указанный идентификатор пользователя текущему аутентифицированному пользователю.
     *
     * @param userId идентификатор пользователя для проверки
     * @return true если указанный идентификатор соответствует текущему пользователю, иначе false
     */
    public boolean isCurrentUser(Long userId) {
        return userService.isCurrentUserById(userId);
    }
}