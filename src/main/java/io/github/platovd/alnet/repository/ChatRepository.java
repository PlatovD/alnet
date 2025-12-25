package io.github.platovd.alnet.repository;

import io.github.platovd.alnet.entity.Chat;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Репозиторий для работы с сущностью Chat.
 * Предоставляет стандартные CRUD операции для работы с чатами.
 *
 * @author PlatovD
 * @version 1.0
 */
public interface ChatRepository extends JpaRepository<Chat, Long> {
}