package io.github.platovd.alnet.controller;

import io.github.platovd.alnet.dto.membership.request.MembershipOperationRequest;
import io.github.platovd.alnet.dto.membership.response.UserMembershipResponse;
import io.github.platovd.alnet.dto.membership.response.ChatMembersResponse;
import io.github.platovd.alnet.dto.membership.response.UserChatsMembershipResponse;
import io.github.platovd.alnet.entity.Chat;
import io.github.platovd.alnet.entity.User;
import io.github.platovd.alnet.service.atomic.ChatService;
import io.github.platovd.alnet.service.atomic.MembershipService;
import io.github.platovd.alnet.service.atomic.UserService;
import io.github.platovd.alnet.service.orchestration.UserFacade;
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
    private final ChatService chatService;
    private final UserFacade userFacade;
    private final UserService userService;

    @Operation(description = "Получить список чатов пользователя")
    @GetMapping
    public UserChatsMembershipResponse getChatsList() {
        return membershipService.getAllMembershipsForUser(userFacade.getCurrentUser());
    }


    @PreAuthorize("membershipSecurity.isMember(#chatId)")
    @Operation(description = "Получить всех членов чата")
    @GetMapping("/{id}")
    public ChatMembersResponse getMembersOfChat(@PathVariable(name = "id") Long chatId) {
        List<User> members = membershipService.getAllMembersOfChat(chatId);
        return new ChatMembersResponse(chatId, members.stream().map(UserMembershipResponse::new).toList());
    }

    @PreAuthorize("membershipSecurity.isMember(#chatId)")
    @Operation(description = "Добавить пользователей в чат")
    @PostMapping("/{id}")
    public ChatMembersResponse addAllMembersToChat(@PathVariable Long chatId, @Valid @RequestBody MembershipOperationRequest request) {

        Chat chat = chatService.getChatById(request.getChatId());
        List<User> usersToAdd = userService.getAllUsersByUsername(request.getUsernames()).stream().filter(
                user -> !membershipService.isMemberOfChat(user.getUserId(), chat.getChatId())
        ).toList();
        membershipService.addAllMembersToChat(usersToAdd, chat);
        return new ChatMembersResponse(chat.getChatId(), usersToAdd.stream().map(UserMembershipResponse::new).toList());
    }

    @PreAuthorize("membershipSecurity.isMember(#chatId)")
    @Operation(description = "Удалить пользователй из чата")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMemberOfChat(@PathVariable Long chatId, @Valid @RequestBody MembershipOperationRequest request) {
        Chat chat = chatService.getChatById(request.getChatId());
        membershipService.removeAllMembersFromChat(request.getUsernames().stream().map(userService::getByUsername).toList(), chat);
        return ResponseEntity.status(204).build();
    }
}
