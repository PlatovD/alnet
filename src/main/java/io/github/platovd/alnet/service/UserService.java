package io.github.platovd.alnet.service;

import io.github.platovd.alnet.entity.User;
import io.github.platovd.alnet.exception.*;
import io.github.platovd.alnet.repository.UserRepository;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository repository;

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

    public List<User> getAllUsersByLogin(List<String> members) {
        Set<String> membersNormalized = members.stream().map(String::strip).collect(Collectors.toSet());
        List<User> users = new ArrayList<>();
        for (String member : membersNormalized) {
            try {
                User user = getByUsername(member);
                users.add(user);
            } catch (UserServiceException ignored) {
            }
        }
        return users;
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

    public void deleteUserById() {

    }
}
