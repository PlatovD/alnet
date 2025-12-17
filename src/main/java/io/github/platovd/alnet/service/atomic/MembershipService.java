package io.github.platovd.alnet.service.atomic;

import io.github.platovd.alnet.entity.Chat;
import io.github.platovd.alnet.entity.User;
import io.github.platovd.alnet.entity.Membership;
import io.github.platovd.alnet.repository.MembershipRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

/**
 * Сервис, который работает с промежуточной таблицей user_chat и связывает пользователей с чатами,
 * в которых они состоят. Выполняет CRUD операции с кортежами данной таблицы.
 */
@Service
@RequiredArgsConstructor
public class MembershipService {
    private final MembershipRepository repository;

    @Transactional
    public void addUserAsMember(User user, Chat chat) {
        if (isMemberOfChat(user.getUserId(), chat.getChatId())) return;
        Membership membership = Membership.builder().user(user).chat(chat).build();
        repository.save(membership);
    }

    @Transactional
    public void addAllMembersToChat(List<User> users, Chat chat) {
        if (users == null || users.isEmpty()) return;
        Set<Long> existingIds = repository.getAllExistingInChatUsersIds(users.stream().map(User::getUserId).toList(), chat.getChatId());
        List<Membership> memberships = users.stream().filter(user -> !existingIds.contains(user.getUserId()))
                .map(user -> Membership.builder().user(user).chat(chat).build()).toList();
        repository.saveAll(memberships);
    }

    @Transactional(readOnly = true)
    public List<Membership> getAllMembershipsForUser(User user) {
        return repository.getAllByUserUserId(user.getUserId());
    }

    @Transactional(readOnly = true)
    public List<User> getAllMembersOfChat(Long chatId) {
        return repository.getAllMembersOfChatByChatId(chatId);
    }

    @Transactional
    public void removeMemberFromChat(User user, Chat chat) {
        repository.deleteByUserUserIdAndChatChatId(user.getUserId(), chat.getChatId());
    }

    @Transactional
    public void removeAllMembersFromChat(List<User> users, Chat chat) {
        if (users == null || users.isEmpty()) return;
        repository.removeAllByUserUserIdAndChatChatId(users.stream().map(User::getUserId).toList(), chat.getChatId());
    }

    @Transactional(readOnly = true)
    public boolean isMemberOfChat(Long userId, Long chatId) {
        return repository.existsByUserUserIdAndChatChatId(userId, chatId);
    }

    @Transactional(readOnly = true)
    protected Long getCountMembersOfChat(Long chatId) {
        return repository.countMembersOfChat(chatId);
    }
}
