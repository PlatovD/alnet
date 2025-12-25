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

/**
 * Контроллер для обработки WebSocket сообщений.
 * Обрабатывает real-time сообщения, отправляемые через WebSocket соединения.
 *
 * @author PlatovD
 * @version 1.0
 */
@Controller
@RequiredArgsConstructor
public class WebSocketController {

    /**
     * Фасад для работы с сообщениями.
     */
    private final MessageFacade messageFacade;

    /**
     * Обрабатывает входящие сообщения от клиентов через WebSocket.
     *
     * @param chatId идентификатор чата, в который отправляется сообщение
     * @param message данные сообщения
     * @param principal объект с информацией об аутентифицированном пользователе
     */
    @MessageMapping("/chats/{chatId}")
    public void incomingFromClientsMessageWebsocket(@DestinationVariable Long chatId, @Valid @Payload MessageRequest message, Principal principal) {
        messageFacade.sendMessageToChat(chatId, message, principal);
    }
}