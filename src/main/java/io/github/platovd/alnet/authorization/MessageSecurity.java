package io.github.platovd.alnet.authorization;

import io.github.platovd.alnet.service.atomic.MessageService;
import io.github.platovd.alnet.service.atomic.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * Компонент безопасности для проверки авторства сообщений.
 * Используется в аннотациях Spring Security для контроля доступа к сообщениям.
 *
 * @author PlatovD
 * @version 1.0
 */
@Component("messageSecurity")
@RequiredArgsConstructor
public class MessageSecurity {

    /**
     * Сервис для работы с пользователями.
     */
    private UserService userService;

    /**
     * Сервис для работы с сообщениями.
     */
    private MessageService messageService;

    /**
     * Проверяет, является ли текущий пользователь автором указанного сообщения.
     *
     * @param messageId идентификатор сообщения для проверки
     * @return true если пользователь является автором сообщения, иначе false
     */
    public boolean isAuthor(Long messageId) {
        return Objects.equals(userService.getCurrentUser().getUserId(), messageService.getMessageAuthor(messageId).getUserId());
    }
}