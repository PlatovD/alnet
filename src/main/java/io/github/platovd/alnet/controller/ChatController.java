package io.github.platovd.alnet.controller;

import io.github.platovd.alnet.dto.chat.request.ChatCreationOrUpdateRequest;
import io.github.platovd.alnet.dto.chat.response.ChatResponse;
import io.github.platovd.alnet.entity.Chat;
import io.github.platovd.alnet.service.atomic.ChatService;
import io.github.platovd.alnet.service.orchestration.ChatFacade;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Контроллер для управления чатами.
 * Предоставляет endpoints для создания, получения, обновления и удаления чатов.
 *
 * @author PlatovD
 * @version 1.0
 */
@RestController
@RequestMapping("/api/chats")
@RequiredArgsConstructor
@Tag(name = "Управление чатами")
public class ChatController {

    /**
     * Сервис для работы с чатами.
     */
    private final ChatService chatService;

    /**
     * Фасад для работы с чатами.
     */
    private final ChatFacade chatFacade;

    /**
     * Получает информацию о чате по его идентификатору.
     * Требуется членство в чате.
     *
     * @param chatId идентификатор чата
     * @return информация о чате
     */
    @PreAuthorize("@membershipSecurity.isMember(#chatId)")
    @GetMapping("/{id}")
    public ChatResponse getChat(@PathVariable("id") Long chatId) {
        Chat chat = chatService.getChatById(chatId);
        return new ChatResponse(chat.getChatId(), chat.getChatName());
    }

    /**
     * Создает новый чат с указанными участниками.
     *
     * @param chatCreationOrUpdateRequest данные для создания чата
     * @return информация о созданном чате
     */
    @PostMapping
    public ChatResponse createChat(@RequestBody @Valid ChatCreationOrUpdateRequest chatCreationOrUpdateRequest) {
        Chat chat = chatFacade.createChatWithMembers(chatCreationOrUpdateRequest);
        return new ChatResponse(chat.getChatId(), chat.getChatName());
    }

    /**
     * Обновляет информацию о чате.
     * Требуется членство в чате.
     *
     * @param chatId идентификатор чата
     * @param chatCreationOrUpdateRequest новые данные для чата
     * @return обновленная информация о чате
     */
    @PreAuthorize("@membershipSecurity.isMember(#chatId)")
    @PutMapping("/{id}")
    public ChatResponse updateChat(@PathVariable("id") Long chatId, @RequestBody @Valid ChatCreationOrUpdateRequest chatCreationOrUpdateRequest) {
        Chat chat = chatService.updateChat(chatId, chatCreationOrUpdateRequest);
        return new ChatResponse(chat.getChatId(), chat.getChatName());
    }

    /**
     * Удаляет чат по его идентификатору.
     * Требуется членство в чате.
     *
     * @param chatId идентификатор чата для удаления
     * @return ответ с HTTP статусом 200 OK
     */
    @PreAuthorize("@membershipSecurity.isMember(#chatId)")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteChat(@PathVariable(name = "id") Long chatId) {
        chatService.deleteChatById(chatId);
        return ResponseEntity.ok().build();
    }
}