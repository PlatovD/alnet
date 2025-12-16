package io.github.platovd.alnet.repository;

import io.github.platovd.alnet.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findUserByUsername(String username);

    @Query("SELECT u FROM User u where u.username in :usernames")
    List<User> findAllByUsernames(@Param("usernames") Collection<String> usernames);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    void removeUserByUserId(Long userId);
}
