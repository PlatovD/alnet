package io.github.platovd.alnet.repository;

import io.github.platovd.alnet.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Репозиторий для работы с сущностью Role.
 * Предоставляет стандартные CRUD операции для работы с ролями пользователей.
 *
 * @author PlatovD
 * @version 1.0
 */
public interface RoleRepository extends JpaRepository<Role, Long> {
}