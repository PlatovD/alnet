package io.github.platovd.alnet.service.atomic;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jsonpatch.JsonPatch;
import com.github.fge.jsonpatch.JsonPatchException;
import io.github.platovd.alnet.entity.User;
import io.github.platovd.alnet.exception.user.*;
import io.github.platovd.alnet.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository repository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final ObjectMapper objectMapper;

    @Transactional
    public User create(String username, String email, String password) {
        if (repository.existsByUsername(username)) {
            throw new UsernameUsedException("Username is already in use");
        }

        if (repository.existsByEmail(email)) {
            throw new EmailUsedException("Email is already in use");
        }
        String encodedPassword = passwordEncoder.encode(password);
        User user = User.builder().username(username).email(email).password(encodedPassword).build();
        return repository.save(user);
    }

    public List<User> getAllUsersByUsername(List<String> members) {
        Set<String> membersNormalized = members.stream().map(String::strip).collect(Collectors.toSet());
        return repository.findAllByUsernames(membersNormalized);
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
