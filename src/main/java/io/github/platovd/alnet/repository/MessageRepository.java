package io.github.platovd.alnet.repository;

import io.github.platovd.alnet.entity.Message;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MessageRepository extends JpaRepository<Message, Long> {
    @Query("select mes FROM Message mes JOIN FETCH mes.user JOIN FETCH mes.chat WHERE mes.chat.chatId =:chatId")
    Slice<Message> getAllMessagesOfChatPaginated(@Param("chatId") Long chatId, Pageable pageable);
}
