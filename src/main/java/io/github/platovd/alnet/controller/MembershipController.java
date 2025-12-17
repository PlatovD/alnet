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

@RestController
@RequestMapping("/api/membership")
@RequiredArgsConstructor
@Tag(name = "Управление членством в чатах")
public class MembershipController {
    private final MembershipService membershipService;
    private final ChatFacade chatFacade;

    @Operation(description = "Получить список чатов пользователя")
    @GetMapping
    public UserChatsMembershipResponse getChatsList() {
        return chatFacade.getChatsListForCurrentUser();
    }

    @PreAuthorize("@membershipSecurity.isMember(#chatId)")
    @Operation(description = "Получить всех членов чата")
    @GetMapping("/{id}")
    public ChatMembersResponse getMembersOfChat(@PathVariable(name = "id") Long chatId) {
        List<User> members = membershipService.getAllMembersOfChat(chatId);
        return new ChatMembersResponse(chatId, members.stream().map(UserMembershipResponse::new).toList());
    }

    @PreAuthorize("@membershipSecurity.isMember(#chatId)")
    @Operation(description = "Добавить пользователей в чат")
    @PostMapping("/{id}")
    public ChatMembersResponse addAllUsersToChat(@PathVariable(name = "id") Long chatId, @Valid @RequestBody MembershipOperationRequest request) {
        return chatFacade.addAllUsersToChat(chatId, request);
    }

    @PreAuthorize("@membershipSecurity.isMember(#chatId)")
    @Operation(description = "Удалить пользователй из чата")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMembersOfChat(@PathVariable(name = "id") Long chatId, @Valid @RequestBody MembershipOperationRequest request) {
        chatFacade.deleteMembersOfChat(chatId, request);
        return ResponseEntity.status(204).build();
    }
}
