package io.github.platovd.alnet.service.orchestration;

import io.github.platovd.alnet.dto.chat.request.ChatCreationOrUpdateRequest;
import io.github.platovd.alnet.entity.Chat;
import io.github.platovd.alnet.entity.User;
import io.github.platovd.alnet.service.atomic.ChatService;
import io.github.platovd.alnet.service.atomic.MembershipService;
import io.github.platovd.alnet.service.atomic.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatFacade {
    private final MembershipService membershipService;
    private final ChatService chatService;
    private final UserService userService;

    @Transactional
    public Chat createChatWithMembers(ChatCreationOrUpdateRequest chatCreationOrUpdateRequest) {
        Chat chat = chatService.createChat(chatCreationOrUpdateRequest.getName());
        List<User> users = userService.getAllUsersByUsername(chatCreationOrUpdateRequest.getMembers());
        membershipService.addAllMembersToChat(users, chat);
        return chat;
    }
}
