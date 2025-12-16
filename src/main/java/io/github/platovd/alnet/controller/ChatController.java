package io.github.platovd.alnet.controller;

import io.github.platovd.alnet.dto.chat.request.ChatCreationOrUpdateRequest;
import io.github.platovd.alnet.dto.chat.response.ChatResponse;
import io.github.platovd.alnet.entity.Chat;
import io.github.platovd.alnet.service.ChatService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/chats")
@RequiredArgsConstructor
@Tag(name = "Управление чатами")
public class ChatController {
    private final ChatService chatService;

    @PreAuthorize("@chatSecurity.isMember(#chatId)")
    @GetMapping("/{id}")
    public ChatResponse getChat(@PathVariable("id") Long chatId) {
        Chat chat = chatService.getChatById(chatId);
        return new ChatResponse(chat.getChatId(), chat.getChatName());
    }

    @PostMapping
    public ChatResponse createChat(@RequestBody @Valid ChatCreationOrUpdateRequest chatCreationOrUpdateRequest) {
        Chat chat = chatService.createChatWithMembers(chatCreationOrUpdateRequest.getName(), chatCreationOrUpdateRequest.getMembers());
        return new ChatResponse(chat.getChatId(), chat.getChatName());
    }

    @PreAuthorize("@chatSecurity.isMember(#chatId)")
    @PutMapping("/{id}")
    public ChatResponse updateChat(@PathVariable("id") Long chatId, @RequestBody @Valid ChatCreationOrUpdateRequest chatCreationOrUpdateRequest) {
        Chat chat = chatService.updateChat(chatId, chatCreationOrUpdateRequest);
        return new ChatResponse(chat.getChatId(), chat.getChatName());
    }

    @PreAuthorize("@chatSecurity.isMember(#chatId)")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteChat(@PathVariable(name = "id") Long chatId) {
        chatService.deleteChatById(chatId);
        return ResponseEntity.ok().build();
    }
}
