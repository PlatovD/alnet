package io.github.platovd.alnet.repository;

import io.github.platovd.alnet.entity.Chat;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatRepository extends JpaRepository<Chat, Long> {
}
