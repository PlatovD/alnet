package io.github.platovd.alnet.repository;

import io.github.platovd.alnet.entity.User;
import io.github.platovd.alnet.entity.Membership;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * Репозиторий для работы с сущностью Membership.
 * Предоставляет операции для управления членством пользователей в чатах.
 *
 * @author PlatovD
 * @version 1.0
 */
public interface MembershipRepository extends JpaRepository<Membership, Long> {

    /**
     * Находит все членства по идентификатору пользователя.
     *
     * @param id идентификатор пользователя
     * @return список членств пользователя
     */
    List<Membership> getAllByUserUserId(Long id);

    /**
     * Проверяет, существует ли членство пользователя в указанном чате.
     *
     * @param userId идентификатор пользователя
     * @param chatId идентификатор чата
     * @return true если пользователь является членом чата, иначе false
     */
    boolean existsByUserUserIdAndChatChatId(Long userId, Long chatId);

    /**
     * Подсчитывает количество участников в указанном чате.
     *
     * @param chat_id идентификатор чата
     * @return количество участников чата
     */
    @Query("SELECT COUNT(*) FROM Membership uc WHERE uc.chat.chatId= :chat_id")
    Long countMembersOfChat(@Param("chat_id") Long chatId);

    /**
     * Удаляет членство пользователя в указанном чате.
     *
     * @param userId идентификатор пользователя
     * @param chatId идентификатор чата
     */
    @Modifying
    void deleteByUserUserIdAndChatChatId(Long userId, Long chatId);

    /**
     * Получает всех участников указанного чата.
     *
     * @param userChatId идентификатор чата
     * @return список пользователей-участников чата
     */
    @Query("SELECT uc.user FROM Membership uc WHERE uc.chat.chatId= :chat_id")
    List<User> getAllMembersOfChatByChatId(@Param("chat_id") Long userChatId);

    /**
     * Находит идентификаторы пользователей, которые уже являются участниками указанного чата.
     *
     * @param userIds коллекция идентификаторов пользователей для проверки
     * @param chatId идентификатор чата
     * @return множество идентификаторов пользователей, уже состоящих в чате
     */
    @Query("SELECT m.user.userId FROM Membership m WHERE m.chat.chatId = :chatId and m.user.userId in :userIds")
    Set<Long> getAllExistingInChatUsersIds(@Param("userIds") Collection<Long> userIds, @Param("chatId") Long chatId);

    /**
     * Удаляет несколько пользователей из указанного чата.
     *
     * @param userIds коллекция идентификаторов пользователей для удаления
     * @param chatId идентификатор чата
     */
    @Modifying(clearAutomatically = true)
    @Query("DELETE FROM Membership m where m.user.userId in :userIds AND m.chat.chatId = :chatId")
    void removeAllByUserUserIdAndChatChatId(@Param("userIds") Collection<Long> userIds, @Param("chatId") Long chatId);
}
