package io.github.platovd.alnet.service;

import io.github.platovd.alnet.entity.User;
import io.github.platovd.alnet.entity.util.Role;
import io.github.platovd.alnet.exception.EmailUsedException;
import io.github.platovd.alnet.exception.UsernameNotFoundException;
import io.github.platovd.alnet.exception.UsernameUsedException;
import io.github.platovd.alnet.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository repository;

    public User save(User user) {
        return repository.save(user);
    }

    public User create(User user) {
        if (repository.existsByUsername(user.getUsername())) {
            throw new UsernameUsedException("Username is already in use");
        }

        if (repository.existsByEmail(user.getEmail())) {
            throw new EmailUsedException("Email is already in use");
        }

        return save(user);
    }

    public User getByUsername(String username) {
        return repository.findUserByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Username wasn't found"));
    }

    public UserDetailsService userDetailsService() {
        return this::getByUsername;
    }

    public User getCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return getByUsername(username);
    }

    @Deprecated
    public void getAdmin() {
        User user = getCurrentUser();
        user.setRole(Role.ROLE_ADMIN);
        save(user);
    }
}
