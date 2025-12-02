package io.github.platovd.alnet.repository;

import io.github.platovd.alnet.entity.User;
import io.github.platovd.alnet.entity.UserChat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface UserChatRepository extends JpaRepository<UserChat, Long> {
    List<UserChat> getAllByUserUserId(Long id);

    boolean existsByUserUserIdAndChatChatId(Long userId, Long chatId);

    @Query("SELECT COUNT(*) FROM UserChat uc WHERE uc.chat.chatId= :chat_id AND uc.user.userId!= :user_id")
    Long countMembers(@Param("chat_id") Long chatId, @Param("user_id") Long userId);

    void deleteByUserUserIdAndChatChatId(Long userId, Long chatId);

    @Query("SELECT uc.user FROM UserChat uc WHERE uc.chat.chatId= :chat_id")
    List<User> getAllMembersOfChatByChatId(@Param("chat_id") Long userChatId);
}
