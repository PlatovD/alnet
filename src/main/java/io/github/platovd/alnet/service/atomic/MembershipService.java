package io.github.platovd.alnet.service.atomic;

import io.github.platovd.alnet.dto.chat.response.ChatResponse;
import io.github.platovd.alnet.dto.membership.response.UserChatsMembershipResponse;
import io.github.platovd.alnet.entity.Chat;
import io.github.platovd.alnet.entity.User;
import io.github.platovd.alnet.entity.Membership;
import io.github.platovd.alnet.mapper.ChatMapper;
import io.github.platovd.alnet.repository.MembershipRepository;
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
public class MembershipService {
    private final MembershipRepository membershipRepository;
    private final ChatMapper chatMapper;

    @Transactional
    public void addUserAsMember(User user, Chat chat) {
        if (isMemberOfChat(user.getUserId(), chat.getChatId())) return;
        Membership membership = Membership.builder().user(user).chat(chat).build();
        membershipRepository.save(membership);
    }

    @Transactional
    public void addAllMembersToChat(List<User> users, Chat chat) {
        for (User user : users) {
            Membership membership = Membership.builder().user(user).chat(chat).build();
            membershipRepository.save(membership);
        }
    }

    @Transactional(readOnly = true)
    public UserChatsMembershipResponse getAllMembershipsForUser(User user) {
        List<Membership> chats = membershipRepository.getAllByUserUserId(user.getUserId());
        Collection<ChatResponse> chatInfos = chatMapper.allToDTO(chats.stream().map(Membership::getChat).toList());
        return new UserChatsMembershipResponse(user.getUsername(), chatInfos);
    }

    @Transactional(readOnly = true)
    public List<User> getAllMembersOfChat(Long chatId) {
        return membershipRepository.getAllMembersOfChatByChatId(chatId);
    }

    @Transactional
    public void removeMemberFromChat(User user, Chat chat) {
        membershipRepository.deleteByUserUserIdAndChatChatId(user.getUserId(), chat.getChatId());
    }

    @Transactional
    public void removeAllMembersFromChat(List<User> users, Chat chat) {
        for (User user : users) {
            removeMemberFromChat(user, chat);
        }
    }

    @Transactional(readOnly = true)
    public boolean isMemberOfChat(Long userId, Long chatId) {
        return membershipRepository.existsByUserUserIdAndChatChatId(userId, chatId);
    }

    @Transactional(readOnly = true)
    protected Long getCountMembersOfChat(Long chatId) {
        return membershipRepository.countMembersOfChat(chatId);
    }
}
