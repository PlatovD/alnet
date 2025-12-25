package io.github.platovd.alnet.service.orchestration;

import io.github.platovd.alnet.dto.message.request.MessageRequest;
import io.github.platovd.alnet.dto.message.response.MessageResponse;
import io.github.platovd.alnet.entity.Chat;
import io.github.platovd.alnet.entity.Message;
import io.github.platovd.alnet.entity.User;
import io.github.platovd.alnet.exception.authentication.UnauthorizedException;
import io.github.platovd.alnet.mapper.MessageMapper;
import io.github.platovd.alnet.service.atomic.ChatService;
import io.github.platovd.alnet.service.atomic.MembershipService;
import io.github.platovd.alnet.service.atomic.MessageService;
import io.github.platovd.alnet.service.atomic.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.Principal;

/**
 * Фасад для работы с сообщениями.
 * Координирует работу нескольких сервисов для выполнения операций с сообщениями.
 *
 * @author PlatovD
 * @version 1.0
 */
@Service
@RequiredArgsConstructor
public class MessageFacade {

    /**
     * Сервис для работы с сообщениями.
     */
    private final MessageService messageService;

    /**
     * Сервис для работы с членством в чатах.
     */
    private final MembershipService membershipService;

    /**
     * Маппер для преобразования сообщений.
     */
    private final MessageMapper messageMapper;

    /**
     * Сервис для работы с пользователями.
     */
    private final UserService userService;

    /**
     * Сервис для работы с чатами.
     */
    private final ChatService chatService;

    /**
     * Шаблон для отправки сообщений через WebSocket.
     */
    private final SimpMessagingTemplate messagingTemplate;

    /**
     * Получает все сообщения из указанного чата с пагинацией.
     *
     * @param chatId идентификатор чата
     * @param pageable параметры пагинации
     * @return срез сообщений с пагинацией
     */
    @Transactional(readOnly = true)
    public Slice<MessageResponse> getAllMessagesOfChat(Long chatId, Pageable pageable) {
        return messageService.getAllChatMessages(chatId, pageable).map(messageMapper::toDTO);
    }

    /**
     * Отправляет сообщение в указанный чат через WebSocket.
     *
     * @param chatId идентификатор чата
     * @param messageRequest данные сообщения
     * @param principal информация об аутентифицированном пользователе
     * @return отправленное сообщение
     * @throws UnauthorizedException если пользователь не является участником чата
     */
    @Transactional
    public MessageResponse sendMessageToChat(Long chatId, MessageRequest messageRequest, Principal principal) {
        // todo: вот здесь по идее должны происходить еще и события с websocket
        User user = userService.getByUsername(principal.getName());
        if (!membershipService.isMemberOfChat(user.getUserId(), chatId))
            throw new UnauthorizedException("No member of chat");
        Chat chat = chatService.getChatById(chatId);
        Message message = messageService.createMessage(messageRequest.getContent(), user, chat);
        MessageResponse dto = messageMapper.toDTO(message);
        messagingTemplate.convertAndSend("/topic/chats/" + chatId, dto);
        return dto;
    }

    /**
     * Обновляет содержание сообщения.
     *
     * @param messageId идентификатор сообщения
     * @param messageRequest новые данные сообщения
     * @return обновленное сообщение
     */
    public MessageResponse updateMessageContent(Long messageId, MessageRequest messageRequest) {
        return messageMapper.toDTO(messageService.updateMessageContent(messageId, messageRequest.getContent()));
    }

    /**
     * Удаляет сообщение.
     *
     * @param messageId идентификатор сообщения
     */
    public void deleteMessage(Long messageId) {
        messageService.deleteMessage(messageId);
    }
}