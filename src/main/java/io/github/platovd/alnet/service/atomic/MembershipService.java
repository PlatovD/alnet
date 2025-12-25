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
 * Сервис, который работает с промежуточной таблицей membership и связывает пользователей с чатами,
 * в которых они состоят. Выполняет CRUD операции с кортежами данной таблицы.
 *
 * @author PlatovD
 * @version 1.0
 */
@Service
@RequiredArgsConstructor
public class MembershipService {

    /**
     * Репозиторий для работы с членством.
     */
    private final MembershipRepository repository;

    /**
     * Добавляет пользователя в чат как участника.
     *
     * @param user пользователь для добавления
     * @param chat чат для добавления пользователя
     */
    @Transactional
    public void addUserAsMember(User user, Chat chat) {
        if (isMemberOfChat(user.getUserId(), chat.getChatId())) return;
        Membership membership = Membership.builder().user(user).chat(chat).build();
        repository.save(membership);
    }

    /**
     * Добавляет нескольких пользователей в чат.
     *
     * @param users список пользователей для добавления
     * @param chat чат для добавления пользователей
     */
    @Transactional
    public void addAllMembersToChat(List<User> users, Chat chat) {
        if (users == null || users.isEmpty()) return;
        Set<Long> existingIds = repository.getAllExistingInChatUsersIds(users.stream().map(User::getUserId).toList(), chat.getChatId());
        List<Membership> memberships = users.stream().filter(user -> !existingIds.contains(user.getUserId()))
                .map(user -> Membership.builder().user(user).chat(chat).build()).toList();
        repository.saveAll(memberships);
    }

    /**
     * Получает все членства пользователя.
     *
     * @param user пользователь
     * @return список членств пользователя
     */
    @Transactional(readOnly = true)
    public List<Membership> getAllMembershipsForUser(User user) {
        return repository.getAllByUserUserId(user.getUserId());
    }

    /**
     * Получает всех участников указанного чата.
     *
     * @param chatId идентификатор чата
     * @return список участников чата
     */
    @Transactional(readOnly = true)
    public List<User> getAllMembersOfChat(Long chatId) {
        return repository.getAllMembersOfChatByChatId(chatId);
    }

    /**
     * Удаляет пользователя из чата.
     *
     * @param user пользователь для удаления
     * @param chat чат из которого удаляется пользователь
     */
    @Transactional
    public void removeMemberFromChat(User user, Chat chat) {
        repository.deleteByUserUserIdAndChatChatId(user.getUserId(), chat.getChatId());
    }

    /**
     * Удаляет несколько пользователей из чата.
     *
     * @param users список пользователей для удаления
     * @param chat чат из которого удаляются пользователи
     */
    @Transactional
    public void removeAllMembersFromChat(List<User> users, Chat chat) {
        if (users == null || users.isEmpty()) return;
        repository.removeAllByUserUserIdAndChatChatId(users.stream().map(User::getUserId).toList(), chat.getChatId());
    }

    /**
     * Проверяет, является ли пользователь участником чата.
     *
     * @param userId идентификатор пользователя
     * @param chatId идентификатор чата
     * @return true если пользователь является участником чата, иначе false
     */
    @Transactional(readOnly = true)
    public boolean isMemberOfChat(Long userId, Long chatId) {
        return repository.existsByUserUserIdAndChatChatId(userId, chatId);
    }

    /**
     * Получает количество участников в чате.
     *
     * @param chatId идентификатор чата
     * @return количество участников чата
     */
    @Transactional(readOnly = true)
    protected Long getCountMembersOfChat(Long chatId) {
        return repository.countMembersOfChat(chatId);
    }
}