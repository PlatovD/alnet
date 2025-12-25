package io.github.platovd.alnet.listener;

import io.github.platovd.alnet.entity.util.UserStatus;
import io.github.platovd.alnet.service.atomic.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.security.Principal;

/**
 * Компонент для обработки событий WebSocket.
 * Отслеживает подключение и отключение пользователей через WebSocket и обновляет их статусы.
 *
 * @author PlatovD
 * @version 1.0
 */
@Component
@RequiredArgsConstructor
public class WebSocketEventListener {

    /**
     * Сервис для работы с пользователями.
     */
    private final UserService userService;

    /**
     * Обрабатывает событие подключения пользователя через WebSocket.
     * Устанавливает статус пользователя в ONLINE.
     *
     * @param event событие подключения WebSocket сессии
     */
    @EventListener
    public void handleWebSocketConnected(SessionConnectedEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        Principal principal = headerAccessor.getUser();
        if (principal == null) return;
        userService.changeUserStatus(UserStatus.ONLINE, principal.getName());
    }

    /**
     * Обрабатывает событие отключения пользователя через WebSocket.
     * Устанавливает статус пользователя в OFFLINE.
     *
     * @param event событие отключения WebSocket сессии
     */
    @EventListener
    public void handleWebSocketDisconnected(SessionDisconnectEvent event) {
        Principal principal = event.getUser();
        if (principal == null) return;
        userService.changeUserStatus(UserStatus.OFFLINE, principal.getName());
    }
}