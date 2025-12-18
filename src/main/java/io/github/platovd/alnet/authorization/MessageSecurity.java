package io.github.platovd.alnet.authorization;

import io.github.platovd.alnet.service.atomic.MessageService;
import io.github.platovd.alnet.service.atomic.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component("messageSecurity")
@RequiredArgsConstructor
public class MessageSecurity {
    private UserService userService;
    private MessageService messageService;

    public boolean isAuthor(Long messageId) {
        return Objects.equals(userService.getCurrentUser().getUserId(), messageService.getMessageAuthor(messageId).getUserId());
    }
}
