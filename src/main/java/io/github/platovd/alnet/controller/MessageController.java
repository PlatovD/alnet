package io.github.platovd.alnet.controller;

import io.github.platovd.alnet.dto.message.request.MessageRequest;
import io.github.platovd.alnet.dto.message.response.MessageResponse;
import io.github.platovd.alnet.service.orchestration.MessageFacade;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/message")
@RequiredArgsConstructor
@Tag(name = "Управление сообщениями в чатах")
public class MessageController {
    private final MessageFacade messageFacade;

    @Operation(description = "Получение всех сообщений из чата с пагинацией")
    @PreAuthorize("@membershipSecurity.isMember(#chatId)")
    @GetMapping("/{id}")
    public Slice<MessageResponse> getAllMessagesOfChat(@PathVariable(name = "id") Long chatId,
                                                       @PageableDefault(size = 100, direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return messageFacade.getAllMessagesOfChat(chatId, pageable);
    }

//    @Operation(description = "Отправка сообщений в определенный чат")
//    @PreAuthorize("@membershipSecurity.isMember(#chatId)")
//    @PostMapping("/{id}")
//    public MessageResponse sendMessageToChat(@PathVariable(name = "id") Long chatId, @Valid @RequestBody MessageRequest request) {
//        return messageFacade.sendMessageToChat(chatId, request);
//    }

    @Operation(description = "Редакция контента сообщения по идентификатору")
    @PreAuthorize("@messageSecurity.isAuthor(#messageId)")
    @PutMapping("/{id}")
    public MessageResponse updateMessageContent(@PathVariable(name = "id") Long messageId, @Valid @RequestBody MessageRequest request) {
        return messageFacade.updateMessageContent(messageId, request);
    }

    @Operation(description = "Удаление сообщения по идентификатору")
    @PreAuthorize("@messageSecurity.isAuthor(#messageId)")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMessage(@PathVariable(name = "id") Long messageId) {
        messageFacade.deleteMessage(messageId);
        return ResponseEntity.status(204).build();
    }
}
