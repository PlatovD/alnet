package io.github.platovd.alnet.controller;

import io.github.platovd.alnet.dto.membership.request.MembershipOperationRequest;
import io.github.platovd.alnet.dto.membership.response.UserMembershipResponse;
import io.github.platovd.alnet.dto.membership.response.ChatMembersResponse;
import io.github.platovd.alnet.dto.membership.response.UserChatsMembershipResponse;
import io.github.platovd.alnet.entity.Chat;
import io.github.platovd.alnet.entity.User;
import io.github.platovd.alnet.service.AuthenticationService;
import io.github.platovd.alnet.service.ChatService;
import io.github.platovd.alnet.service.MembershipService;
import io.github.platovd.alnet.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/membership")
@RequiredArgsConstructor
@Tag(name = "Управление членством в чатах")
public class MembershipController {
    private final MembershipService membershipService;
    private final ChatService chatService;
    private final AuthenticationService authenticationService;
    private final UserService userService;

    @Operation(description = "Получить список чатов пользователя")
    @GetMapping
    public UserChatsMembershipResponse getChatsList() {
        return membershipService.getAllMembershipsForUser(authenticationService.getCurrentUser());
    }

    @Operation(description = "Получить всех членов чата")
    @GetMapping("/{id}")
    public ChatMembersResponse getMembersOfChat(@PathVariable(name = "id") Long chatId) {
        if (!membershipService.isMemberOfChat(authenticationService.getCurrentUser(), chatService.getChatById(chatId))) {
            throw new AuthorizationDeniedException("Current user isn't member of requested chat");
        }
        List<User> members = membershipService.getAllMembersOfChat(chatId);
        return new ChatMembersResponse(chatId, members.stream().map(UserMembershipResponse::new).toList());
    }

    @Operation(description = "Добавить пользователей в чат")
    @PostMapping
    public ChatMembersResponse addAllMembersToChat(@Valid @RequestBody MembershipOperationRequest request) {
        Chat chat = chatService.getChatById(request.getChatId());
        if (!membershipService.isMemberOfChat(authenticationService.getCurrentUser(), chat)) {
            throw new AuthorizationDeniedException("Current user isn't member of requested chat");
        }
        List<User> usersToAdd = userService.getAllUsersByLogin(request.getUsernames()).stream().filter(
                user -> !membershipService.isMemberOfChat(user, chat)
        ).toList();
        membershipService.addAllMembersToChat(usersToAdd, chat);
        return new ChatMembersResponse(chat.getChatId(), usersToAdd.stream().map(UserMembershipResponse::new).toList());
    }

    @Operation(description = "Удалить пользователй из чата")
    @DeleteMapping
    public ResponseEntity<Void> deleteMemberOfChat(@Valid @RequestBody MembershipOperationRequest request) {
        Chat chat = chatService.getChatById(request.getChatId());
        if (!membershipService.isMemberOfChat(authenticationService.getCurrentUser(), chat)) {
            throw new AuthorizationDeniedException("Current user isn't member of requested chat");
        }
        membershipService.removeAllMembersFromChat(request.getUsernames().stream().map(userService::getByUsername).toList(), chat);
        return ResponseEntity.status(204).build();
    }
}
