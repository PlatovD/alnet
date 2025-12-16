package io.github.platovd.alnet.repository;

import io.github.platovd.alnet.entity.User;
import io.github.platovd.alnet.entity.Membership;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MembershipRepository extends JpaRepository<Membership, Long> {
    List<Membership> getAllByUserUserId(Long id);

    boolean existsByUserUserIdAndChatChatId(Long userId, Long chatId);

    @Query("SELECT COUNT(*) FROM Membership uc WHERE uc.chat.chatId= :chat_id")
    Long countMembersOfChat(@Param("chat_id") Long chatId);

    void deleteByUserUserIdAndChatChatId(Long userId, Long chatId);

    @Query("SELECT uc.user FROM Membership uc WHERE uc.chat.chatId= :chat_id")
    List<User> getAllMembersOfChatByChatId(@Param("chat_id") Long userChatId);
}
