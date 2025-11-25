package io.github.platovd.alnet.controller;

import io.github.platovd.alnet.dto.chat.request.ChatCreationOrUpdateRequest;
import io.github.platovd.alnet.dto.chat.response.ChatResponse;
import io.github.platovd.alnet.dto.userchat.response.ChatsInfoResponse;
import io.github.platovd.alnet.entity.Chat;
import io.github.platovd.alnet.service.AuthenticationService;
import io.github.platovd.alnet.service.ChatService;
import io.github.platovd.alnet.service.UserChatService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/chats")
@RequiredArgsConstructor
@Tag(name = "Управление чатами")
public class ChatController {
    private final ChatService chatService;
    private final UserChatService userChatService;
    private final AuthenticationService authenticationService;

    @GetMapping
    public ChatsInfoResponse getChatsList() {
        return userChatService.getAllChatsForUser(authenticationService.getCurrentUser());
    }

    @GetMapping("/{id}")
    public ChatResponse getChat(@PathVariable("id") Long chatId) {
        Chat chat = chatService.getChatById(chatId);
        if (!userChatService.isMemberOfChat(authenticationService.getCurrentUser(), chatService.getChatById(chatId))) {
            throw new AuthorizationDeniedException("Current user isn't member of requested chat");
        }
        return new ChatResponse(chat.getChatId(), chat.getChatName());
    }

    @PostMapping
    public ChatResponse createChat(@RequestBody @Valid ChatCreationOrUpdateRequest chatCreationOrUpdateRequest) {
        Chat chat = chatService.createChat(chatCreationOrUpdateRequest.getName());
        userChatService.addAllMembersToChat(chatCreationOrUpdateRequest.getMembers(), chat);
        return new ChatResponse(chat.getChatId(), chat.getChatName());
    }

    @PutMapping
    public ChatResponse updateChat(@RequestBody @Valid ChatCreationOrUpdateRequest chatCreationOrUpdateRequest) {
        Chat chat = chatService.updateChat(chatCreationOrUpdateRequest);
        if (!userChatService.isMemberOfChat(authenticationService.getCurrentUser(), chat)) {
            throw new AuthorizationDeniedException("Current user isn't member of requested chat");
        }
        return new ChatResponse(chat.getChatId(), chat.getChatName());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteChat(@PathVariable(name = "id") Long chatId) {
        Chat chat = chatService.getChatById(chatId);
        if (!userChatService.isMemberOfChat(authenticationService.getCurrentUser(), chat)) {
            throw new AuthorizationDeniedException("Current user isn't member of requested chat");
        }
        chatService.deleteChat(chat);
        return ResponseEntity.ok().build();
    }
}
