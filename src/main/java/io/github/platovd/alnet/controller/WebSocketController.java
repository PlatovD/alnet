package io.github.platovd.alnet.controller;

import io.github.platovd.alnet.dto.message.request.MessageRequest;
import io.github.platovd.alnet.service.orchestration.MessageFacade;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Controller
@RequiredArgsConstructor
public class WebSocketController {
    private final MessageFacade messageFacade;

    @MessageMapping("/chats.{chatId}")
    public void incomingFromClientsMessageWebsocket(@DestinationVariable Long chatId, @Valid @Payload MessageRequest message, Principal principal) {
        messageFacade.sendMessageToChat(chatId, message, principal);
    }
}
