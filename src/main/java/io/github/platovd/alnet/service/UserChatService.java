package io.github.platovd.alnet.service;

import io.github.platovd.alnet.dto.userchat.response.UserChatsResponse;
import io.github.platovd.alnet.entity.Chat;
import io.github.platovd.alnet.entity.User;
import io.github.platovd.alnet.entity.UserChat;
import io.github.platovd.alnet.mapper.ChatMapper;
import io.github.platovd.alnet.mapper.info.ChatInfoDTO;
import io.github.platovd.alnet.repository.UserChatRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;

/**
 * Сервис, который работает с промежуточной таблицей user_chat и связывает пользователей с чатами,
 * в которых они состоят. Выполняет CRUD операции с кортежами данной таблицы.
 */
@Service
@RequiredArgsConstructor
public class UserChatService {
    private final UserChatRepository userChatRepository;
    private final ChatMapper chatMapper;

    @Transactional
    public void addUserToChat(User user, Chat chat) {
        if (isMemberOfChat(user, chat)) return;
        UserChat userChat = UserChat.builder().user(user).chat(chat).build();
        userChatRepository.save(userChat);
    }

    @Transactional
    public void addAllMembersToChat(List<User> users, Chat chat) {
        for (User user : users) {
            UserChat userChat = UserChat.builder().user(user).chat(chat).build();
            userChatRepository.save(userChat);
        }
    }

    @Transactional(readOnly = true)
    public UserChatsResponse getAllChatsForUser(User user) {
        List<UserChat> chats = userChatRepository.getAllByUserUserId(user.getUserId());
        Collection<ChatInfoDTO> chatInfos = chatMapper.allToInfo(chats.stream().map(UserChat::getChat).toList());
        return new UserChatsResponse(user.getUsername(), chatInfos);
    }

    @Transactional(readOnly = true)
    public List<User> getAllMembersOfChat(Long chatId) {
        return userChatRepository.getAllMembersOfChatByChatId(chatId);
    }

    @Transactional
    public void removeUserFromChat(User user, Chat chat) {
        // проверять, что пользователь не последний
        userChatRepository.deleteByUserUserIdAndChatChatId(user.getUserId(), chat.getChatId());
    }

    @Transactional(readOnly = true)
    public boolean isMemberOfChat(User user, Chat chat) {
        return userChatRepository.existsByUserUserIdAndChatChatId(user.getUserId(), chat.getChatId());
    }

    @Transactional(readOnly = true)
    private Long getCountMembersOfChat(Long chatId, Long userId) {
        return userChatRepository.countMembers(chatId, userId);
    }
}
