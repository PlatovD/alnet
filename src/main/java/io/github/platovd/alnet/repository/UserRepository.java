package io.github.platovd.alnet.repository;

import io.github.platovd.alnet.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * Репозиторий для работы с сущностью User.
 * Предоставляет операции для работы с пользователями системы.
 *
 * @author PlatovD
 * @version 1.0
 */
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Находит пользователя по имени пользователя.
     *
     * @param username имя пользователя для поиска
     * @return Optional с найденным пользователем или пустой Optional
     */
    Optional<User> findUserByUsername(String username);

    /**
     * Находит всех пользователей по коллекции имен пользователей.
     *
     * @param usernames коллекция имен пользователей для поиска
     * @return список найденных пользователей
     */
    @Query("SELECT u FROM User u where u.username in :usernames")
    List<User> findAllByUsernames(@Param("usernames") Collection<String> usernames);

    /**
     * Проверяет, существует ли пользователь с указанным именем пользователя.
     *
     * @param username имя пользователя для проверки
     * @return true если пользователь существует, иначе false
     */
    boolean existsByUsername(String username);

    /**
     * Проверяет, существует ли пользователь с указанным email.
     *
     * @param email email для проверки
     * @return true если пользователь с таким email существует, иначе false
     */
    boolean existsByEmail(String email);

    /**
     * Удаляет пользователя по его идентификатору.
     *
     * @param userId идентификатор пользователя для удаления
     */
    void removeUserByUserId(Long userId);
}