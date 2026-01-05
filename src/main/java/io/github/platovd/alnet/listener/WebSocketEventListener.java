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

@Component
@RequiredArgsConstructor
public class WebSocketEventListener {
    private final UserService userService;

    @EventListener
    public void handleWebSocketConnected(SessionConnectedEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        Principal principal = headerAccessor.getUser();
        if (principal == null) return;
        userService.changeUserStatus(UserStatus.ONLINE, principal.getName());
    }

    @EventListener
    public void handleWebSocketDisconnected(SessionDisconnectEvent event) {
        Principal principal = event.getUser();
        if (principal == null) return;
        userService.changeUserStatus(UserStatus.OFFLINE, principal.getName());
    }
}
