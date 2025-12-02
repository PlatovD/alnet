package io.github.platovd.alnet.controller;

import io.github.platovd.alnet.dto.userchat.request.UserChatOperationRequest;
import io.github.platovd.alnet.dto.userchat.response.ChatMemberDTO;
import io.github.platovd.alnet.dto.userchat.response.ChatMembersResponse;
import io.github.platovd.alnet.dto.userchat.response.UserChatsResponse;
import io.github.platovd.alnet.entity.Chat;
import io.github.platovd.alnet.entity.User;
import io.github.platovd.alnet.service.AuthenticationService;
import io.github.platovd.alnet.service.ChatService;
import io.github.platovd.alnet.service.UserChatService;
import io.github.platovd.alnet.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/membership")
@RequiredArgsConstructor
public class UserChatController {
    private final UserChatService userChatService;
    private final ChatService chatService;
    private final AuthenticationService authenticationService;
    private final UserService userService;

    @GetMapping
    public UserChatsResponse getChatsList() {
        return userChatService.getAllChatsForUser(authenticationService.getCurrentUser());
    }

    @GetMapping("/{id}")
    public ChatMembersResponse getMembersOfChat(@PathVariable(name = "id") Long chatId) {
        if (!userChatService.isMemberOfChat(authenticationService.getCurrentUser(), chatService.getChatById(chatId))) {
            throw new AuthorizationDeniedException("Current user isn't member of requested chat");
        }
        List<User> members = userChatService.getAllMembersOfChat(chatId);
        return new ChatMembersResponse(chatId, members.stream().map(ChatMemberDTO::new).toList());
    }

    @PostMapping
    public ChatMembersResponse addAllMembersToChat(@RequestBody UserChatOperationRequest request) {
        Chat chat = chatService.getChatById(request.getChatId());
        if (!userChatService.isMemberOfChat(authenticationService.getCurrentUser(), chat)) {
            throw new AuthorizationDeniedException("Current user isn't member of requested chat");
        }
        List<User> usersToAdd = userService.getAllUsersByLogin(request.getUsernames()).stream().filter(
                user -> !userChatService.isMemberOfChat(user, chat)
        ).toList();
        userChatService.addAllMembersToChat(usersToAdd, chat);
        return new ChatMembersResponse(chat.getChatId(), usersToAdd.stream().map(ChatMemberDTO::new).toList());
    }
}
