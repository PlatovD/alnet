package io.github.platovd.alnet.service;

import io.github.platovd.alnet.dto.userchat.response.ChatsInfoResponse;
import io.github.platovd.alnet.entity.Chat;
import io.github.platovd.alnet.entity.User;
import io.github.platovd.alnet.entity.UserChat;
import io.github.platovd.alnet.exception.UserServiceException;
import io.github.platovd.alnet.mapper.ChatMapper;
import io.github.platovd.alnet.mapper.info.ChatInfo;
import io.github.platovd.alnet.repository.UserChatRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Сервис, который работает с промежуточной таблицей user_chat и связывает пользователей с чатами,
 * в которых они состоят. Выполняет CRUD операции с кортежами данной таблицы.
 */
@Service
@RequiredArgsConstructor
public class UserChatService {
    private final UserChatRepository userChatRepository;
    private final AuthenticationService authenticationService;
    private final ChatService chatService;
    private final UserService userService;
    private final ChatMapper chatMapper;

    @Transactional
    public void addUserToChat(User user, Chat chat) {
        if (isMemberOfChat(user, chat)) return;
        UserChat userChat = UserChat.builder().user(user).chat(chat).build();
        userChatRepository.save(userChat);
    }

    @Transactional
    public void addAllMembersToChat(Collection<String> members, Chat chat) {
        Set<String> membersNormalized = members.stream().map(String::strip).collect(Collectors.toSet());
        for (String username : membersNormalized) {
            try {
                User user = userService.getByUsername(username);
                if (isMemberOfChatUnchecked(user, chat)) continue;
                UserChat userChat = UserChat.builder().user(user).chat(chat).build();
                userChatRepository.save(userChat);
            } catch (UserServiceException ignored) {
            }
        }
    }

    @Transactional(readOnly = true)
    public ChatsInfoResponse getAllChatsForUser(User user) {
        List<UserChat> chats = userChatRepository.getAllByUserUserId(user.getUserId());
        Collection<ChatInfo> chatInfos = chatMapper.allToInfo(chats.stream().map(UserChat::getChat).toList());
        return new ChatsInfoResponse(user.getUsername(), chatInfos);
    }

    @Transactional
    public void removeUserFromChat(User user, Chat chat) {
        if (!authenticationService.isCurrentUser(user))
            throw new AuthorizationDeniedException("Current user doesn't have enough rights to remove user from chat");

        // проверять, что пользователь не последний
        if (getCountMembersOfChat(chat.getChatId(), user.getUserId()) == 1) {
            chatService.deleteChat(chat);
        }
        userChatRepository.deleteByUserUserIdAndChatChatId(user.getUserId(), chat.getChatId());
    }

    public boolean isMemberOfChat(User user, Chat chat) {
        if (!authenticationService.isCurrentUser(user))
            throw new AuthorizationDeniedException("Current user doesn't have enough rights to see membership");
        return isMemberOfChatUnchecked(user, chat);
    }

    private boolean isMemberOfChatUnchecked(User user, Chat chat) {
        return userChatRepository.existsByUserUserIdAndChatChatId(user.getUserId(), chat.getChatId());
    }

    private Long getCountMembersOfChat(Long chatId, Long userId) {
        return userChatRepository.countMembers(chatId, userId);
    }
}
