package io.github.platovd.alnet.service.orchestration;

import io.github.platovd.alnet.dto.chat.request.ChatCreationOrUpdateRequest;
import io.github.platovd.alnet.dto.membership.request.MembershipOperationRequest;
import io.github.platovd.alnet.dto.membership.response.ChatMembersResponse;
import io.github.platovd.alnet.dto.membership.response.UserChatsMembershipResponse;
import io.github.platovd.alnet.dto.membership.response.UserMembershipResponse;
import io.github.platovd.alnet.entity.Chat;
import io.github.platovd.alnet.entity.Membership;
import io.github.platovd.alnet.entity.User;
import io.github.platovd.alnet.exception.general.WrongDataException;
import io.github.platovd.alnet.mapper.ChatMapper;
import io.github.platovd.alnet.service.atomic.ChatService;
import io.github.platovd.alnet.service.atomic.MembershipService;
import io.github.platovd.alnet.service.atomic.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class ChatFacade {
    private final MembershipService membershipService;
    private final ChatService chatService;
    private final UserService userService;
    private final ChatMapper chatMapper;

    @Transactional
    public Chat createChatWithMembers(ChatCreationOrUpdateRequest chatCreationOrUpdateRequest) {
        Chat chat = chatService.createChat(chatCreationOrUpdateRequest.getName());
        List<User> users = userService.getAllUsersByUsername(chatCreationOrUpdateRequest.getMembers());
        membershipService.addAllMembersToChat(users, chat);
        return chat;
    }

    public UserChatsMembershipResponse getChatsListForCurrentUser() {
        User currentUser = userService.getCurrentUser();
        List<Membership> memberships = membershipService.getAllMembershipsForUser(currentUser);
        return UserChatsMembershipResponse.builder().username(currentUser.getUsername()).chats(
                chatMapper.allToDTO(memberships.stream().map(Membership::getChat).toList())
        ).build();
    }

    public ChatMembersResponse addAllUsersToChat(Long chatId, MembershipOperationRequest request) {
        if (!Objects.equals(chatId, request.getChatId()))
            throw new WrongDataException("Url param and request body have conflict information");
        Chat chat = chatService.getChatById(request.getChatId());
        List<User> usersToAdd = userService.getAllUsersByUsername(request.getUsernames());
        membershipService.addAllMembersToChat(usersToAdd, chat);
        return new ChatMembersResponse(chat.getChatId(), usersToAdd.stream().map(UserMembershipResponse::new).toList());
    }

    public void deleteMembersOfChat(Long chatId, MembershipOperationRequest request) {
        if (!Objects.equals(chatId, request.getChatId()))
            throw new WrongDataException("Url param and request body have conflict information");
        Chat chat = chatService.getChatById(request.getChatId());
        List<User> users = userService.getAllUsersByUsername(request.getUsernames());
        membershipService.removeAllMembersFromChat(users, chat);
    }
}
