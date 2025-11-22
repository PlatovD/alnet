package io.github.platovd.alnet.service;

import io.github.platovd.alnet.authentication.contex.SecurityContextWrapper;
import io.github.platovd.alnet.authentication.token.JWTAuthToken;
import io.github.platovd.alnet.entity.User;
import io.github.platovd.alnet.exception.*;
import io.github.platovd.alnet.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository repository;
    private final SecurityContextWrapper securityContextWrapper;

    protected void save(User user) {
        repository.save(user);
    }

    @Transactional
    public void create(User user) {
        if (repository.existsByUsername(user.getUsername())) {
            throw new UsernameUsedException("Username is already in use");
        }

        if (repository.existsByEmail(user.getEmail())) {
            throw new EmailUsedException("Email is already in use");
        }

        save(user);
    }

    @Transactional(readOnly = true)
    public User getByUsername(String username) {
        return repository.findUserByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Username wasn't found"));
    }

    @Transactional(readOnly = true)
    public User getById(Long id) {
        return repository.findById(id).orElseThrow(() -> new IdNotFoundException(("Id wasn't found")));
    }

    @Transactional(readOnly = true)
    public User getCurrentUser() {
        if (!securityContextWrapper.isAuthenticated())
            throw new UserServiceException("No authentication found. Current authentication is " +
                    securityContextWrapper.getAuthentication());
        Authentication authentication = securityContextWrapper.getAuthentication();
        if (authentication instanceof JWTAuthToken jwtAuthToken) {
            return getById(jwtAuthToken.getId());
        }
        return getByUsername(authentication.getName());
    }
}
