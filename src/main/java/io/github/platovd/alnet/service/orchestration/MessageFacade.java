package io.github.platovd.alnet.service.orchestration;

import io.github.platovd.alnet.dto.message.request.MessageRequest;
import io.github.platovd.alnet.dto.message.response.MessageResponse;
import io.github.platovd.alnet.entity.Chat;
import io.github.platovd.alnet.entity.Message;
import io.github.platovd.alnet.entity.User;
import io.github.platovd.alnet.exception.entity.authentication.UnauthorizedException;
import io.github.platovd.alnet.mapper.MessageMapper;
import io.github.platovd.alnet.service.atomic.ChatService;
import io.github.platovd.alnet.service.atomic.MembershipService;
import io.github.platovd.alnet.service.atomic.MessageService;
import io.github.platovd.alnet.service.atomic.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.Principal;

@Service
@RequiredArgsConstructor
public class MessageFacade {
    private final MessageService messageService;
    private final MembershipService membershipService;
    private final MessageMapper messageMapper;
    private final UserService userService;
    private final ChatService chatService;
    private final SimpMessagingTemplate messagingTemplate;

    @Transactional(readOnly = true)
    public Slice<MessageResponse> getAllMessagesOfChat(Long chatId, Pageable pageable) {
        return messageService.getAllChatMessages(chatId, pageable).map(messageMapper::toDTO);
    }

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

    public MessageResponse updateMessageContent(Long messageId, MessageRequest messageRequest) {
        return messageMapper.toDTO(messageService.updateMessageContent(messageId, messageRequest.getContent()));
    }

    public void deleteMessage(Long messageId) {
        messageService.deleteMessage(messageId);
    }
}
