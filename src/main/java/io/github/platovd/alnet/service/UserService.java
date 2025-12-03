package io.github.platovd.alnet.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jsonpatch.JsonPatch;
import com.github.fge.jsonpatch.JsonPatchException;
import io.github.platovd.alnet.entity.User;
import io.github.platovd.alnet.exception.user.*;
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
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;

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
    public User getById(Long userId) {
        return repository.findById(userId).orElseThrow(() -> new IdNotFoundException(("Id wasn't found")));
    }

    @Transactional
    public User updateFullUser(Long userId, String username, String email) {
        User user = getById(userId);
        user.setUsername(username);
        user.setEmail(email);
        return userRepository.save(user);
    }

    @Transactional
    public User applyPatchToUser(JsonPatch patch, User targetUser) throws JsonPatchException, JsonProcessingException {
        JsonNode patched = patch.apply(objectMapper.convertValue(targetUser, JsonNode.class));
        User patchedUser = objectMapper.treeToValue(patched, User.class);

        if (patchedUser == null)
            throw new IllegalArgumentException("Wrong patch request");

        patchedUser.setUserId(targetUser.getUserId());
        patchedUser.setPassword(targetUser.getPassword());

        if (!patchedUser.getUsername().equals(targetUser.getUsername()))
            if (userRepository.existsByUsername(patchedUser.getUsername()))
                throw new UsernameUsedException("Username is already in use");
        if (!patchedUser.getEmail().equals(targetUser.getEmail())) {
            if (userRepository.existsByEmail(patchedUser.getEmail()))
                throw new EmailUsedException("Email is already in use");
        }
        return repository.save(patchedUser);
    }

    @Transactional
    public void deleteUserById(Long userId) {
        repository.removeUserByUserId(userId);
    }
}
