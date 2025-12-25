package io.github.platovd.alnet.controller;

import io.github.platovd.alnet.dto.membership.request.MembershipOperationRequest;
import io.github.platovd.alnet.dto.membership.response.UserMembershipResponse;
import io.github.platovd.alnet.dto.membership.response.ChatMembersResponse;
import io.github.platovd.alnet.dto.membership.response.UserChatsMembershipResponse;
import io.github.platovd.alnet.entity.Chat;
import io.github.platovd.alnet.entity.User;
import io.github.platovd.alnet.service.atomic.MembershipService;
import io.github.platovd.alnet.service.orchestration.ChatFacade;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Контроллер для управления членством пользователей в чатах.
 * Предоставляет endpoints для работы с участниками чатов.
 *
 * @author PlatovD
 * @version 1.0
 */
@RestController
@RequestMapping("/api/membership")
@RequiredArgsConstructor
@Tag(name = "Управление членством в чатах")
public class MembershipController {

    /**
     * Сервис для работы с членством в чатах.
     */
    private final MembershipService membershipService;

    /**
     * Фасад для работы с чатами.
     */
    private final ChatFacade chatFacade;

    /**
     * Получает список чатов текущего пользователя.
     *
     * @return список чатов пользователя
     */
    @Operation(description = "Получить список чатов пользователя")
    @GetMapping
    public UserChatsMembershipResponse getChatsList() {
        return chatFacade.getChatsListForCurrentUser();
    }

    /**
     * Получает всех участников указанного чата.
     * Требуется членство в чате.
     *
     * @param chatId идентификатор чата
     * @return список участников чата
     */
    @PreAuthorize("@membershipSecurity.isMember(#chatId)")
    @Operation(description = "Получить всех членов чата")
    @GetMapping("/{id}")
    public ChatMembersResponse getMembersOfChat(@PathVariable(name = "id") Long chatId) {
        List<User> members = membershipService.getAllMembersOfChat(chatId);
        return new ChatMembersResponse(chatId, members.stream().map(UserMembershipResponse::new).toList());
    }

    /**
     * Добавляет пользователей в указанный чат.
     * Требуется членство в чате.
     *
     * @param chatId идентификатор чата
     * @param request запрос с идентификаторами пользователей для добавления
     * @return обновленный список участников чата
     */
    @PreAuthorize("@membershipSecurity.isMember(#chatId)")
    @Operation(description = "Добавить пользователей в чат")
    @PostMapping("/{id}")
    public ChatMembersResponse addAllUsersToChat(@PathVariable(name = "id") Long chatId, @Valid @RequestBody MembershipOperationRequest request) {
        return chatFacade.addAllUsersToChat(chatId, request);
    }

    /**
     * Удаляет пользователей из указанного чата.
     * Требуется членство в чате.
     *
     * @param chatId идентификатор чата
     * @param request запрос с идентификаторами пользователей для удаления
     * @return ответ с HTTP статусом 204 No Content
     */
    @PreAuthorize("@membershipSecurity.isMember(#chatId)")
    @Operation(description = "Удалить пользователей из чата")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMembersOfChat(@PathVariable(name = "id") Long chatId, @Valid @RequestBody MembershipOperationRequest request) {
        chatFacade.deleteMembersOfChat(chatId, request);
        return ResponseEntity.status(204).build();
    }
}