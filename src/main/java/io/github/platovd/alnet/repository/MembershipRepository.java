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

public interface MembershipRepository extends JpaRepository<Membership, Long> {
    List<Membership> getAllByUserUserId(Long id);

    boolean existsByUserUserIdAndChatChatId(Long userId, Long chatId);

    @Query("SELECT COUNT(*) FROM Membership uc WHERE uc.chat.chatId= :chat_id")
    Long countMembersOfChat(@Param("chat_id") Long chatId);

    @Modifying
    void deleteByUserUserIdAndChatChatId(Long userId, Long chatId);

    @Query("SELECT uc.user FROM Membership uc WHERE uc.chat.chatId= :chat_id")
    List<User> getAllMembersOfChatByChatId(@Param("chat_id") Long userChatId);

    @Query("SELECT m.user.userId FROM Membership m WHERE m.chat.chatId = :chatId and m.user.userId in :userIds")
    Set<Long> getAllExistingInChatUsersIds(@Param("userIds") Collection<Long> userIds, @Param("chatId") Long chatId);

    @Modifying(clearAutomatically = true)
    @Query("DELETE FROM Membership m where m.user.userId in :userIds AND m.chat.chatId = :chatId")
    void removeAllByUserUserIdAndChatChatId(@Param("userIds") Collection<Long> userIds, @Param("chatId") Long chatId);
}
