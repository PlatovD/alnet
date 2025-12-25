package io.github.platovd.alnet.authorization;

import io.github.platovd.alnet.service.atomic.MembershipService;
import io.github.platovd.alnet.service.atomic.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Компонент безопасности для проверки членства пользователя в чатах.
 * Используется в аннотациях Spring Security для контроля доступа.
 *
 * @author PlatovD
 * @version 1.0
 */
@Component("membershipSecurity")
@RequiredArgsConstructor
public class MembershipSecurity {

    /**
     * Сервис для работы с пользователями.
     */
    private final UserService userService;

    /**
     * Сервис для работы с членством в чатах.
     */
    private final MembershipService membershipService;

    /**
     * Проверяет, является ли текущий пользователь членом указанного чата.
     *
     * @param chatId идентификатор чата для проверки
     * @return true если пользователь является членом чата, иначе false
     */
    public boolean isMember(Long chatId) {
        return membershipService.isMemberOfChat(userService.getCurrentUser().getUserId(), chatId);
    }
}