package io.github.platovd.alnet.repository;

import io.github.platovd.alnet.entity.Message;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MessageRepository extends JpaRepository<Message, Long> {
}
