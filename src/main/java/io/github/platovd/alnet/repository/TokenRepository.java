package io.github.platovd.alnet.repository;

import io.github.platovd.alnet.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TokenRepository extends JpaRepository<RefreshToken, Long> {
}
