package io.github.platovd.alnet.service;

import io.github.platovd.alnet.entity.User;
import io.github.platovd.alnet.exception.EmailUsedException;
import io.github.platovd.alnet.exception.IdNotFoundException;
import io.github.platovd.alnet.exception.UsernameNotFoundException;
import io.github.platovd.alnet.exception.UsernameUsedException;
import io.github.platovd.alnet.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository repository;

    public void save(User user) {
        repository.save(user);
    }

    public void create(User user) {
        if (repository.existsByUsername(user.getUsername())) {
            throw new UsernameUsedException("Username is already in use");
        }

        if (repository.existsByEmail(user.getEmail())) {
            throw new EmailUsedException("Email is already in use");
        }

        save(user);
    }

    public User getByUsername(String username) {
        return repository.findUserByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Username wasn't found"));
    }

    public User getCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();

        return getByUsername(username);
    }

    public User getById(Long id) {
        return repository.findById(id).orElseThrow(() -> new IdNotFoundException(("Id wasn't found")));
    }
}
