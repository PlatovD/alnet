package io.github.platovd.alnet.authentication.userdetail;

import io.github.platovd.alnet.authentication.util.AuthUtil;
import io.github.platovd.alnet.repository.UserRepository;
import io.github.platovd.alnet.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Пользовательская реализация сервиса для загрузки данных пользователей.
 * Используется Spring Security для получения информации о пользователе по имени.
 *
 * @author PlatovD
 * @version 1.0
 */
@RequiredArgsConstructor
@Service
public class CustomUserDetailService implements UserDetailsService {

    /**
     * Репозиторий для доступа к данным пользователей.
     */
    private final UserRepository userRepository;

    /**
     * Загружает данные пользователя по его имени.
     *
     * @param username имя пользователя для поиска
     * @return объект UserDetails с информацией о пользователе
     * @throws UsernameNotFoundException если пользователь с указанным именем не найден
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<User> user = userRepository.findUserByUsername(username);
        if (user.isEmpty())
            throw new UsernameNotFoundException("User " + username + " not found in database");
        return AuthUtil.fromUserToUserDetails(user.get());
    }
}