package io.github.platovd.alnet.service.atomic;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jsonpatch.JsonPatch;
import com.github.fge.jsonpatch.JsonPatchException;
import io.github.platovd.alnet.authentication.contex.SecurityContextWrapper;
import io.github.platovd.alnet.authentication.token.JWTAuthToken;
import io.github.platovd.alnet.entity.User;
import io.github.platovd.alnet.entity.util.UserStatus;
import io.github.platovd.alnet.exception.user.*;
import io.github.platovd.alnet.mapper.UserMapper;
import io.github.platovd.alnet.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final ObjectMapper objectMapper;
    private final SecurityContextWrapper securityContextWrapper;

    @Transactional
    public User create(String username, String email, String password) {
        if (repository.existsByUsername(username)) {
            throw new UsernameUsedException("Username is already in use");
        }

        if (repository.existsByEmail(email)) {
            throw new EmailUsedException("Email is already in use");
        }
        String encodedPassword = passwordEncoder.encode(password);
        User user = User.builder().username(username).email(email).password(encodedPassword).status(UserStatus.OFFLINE)
                .build();
        return repository.save(user);
    }

    public List<User> getAllUsersByUsername(List<String> members) {
        Set<String> membersNormalized = members.stream().map(String::strip).filter(s -> !s.isEmpty()).collect(Collectors.toSet());
        if (membersNormalized.isEmpty()) return new ArrayList<>();
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
        return repository.save(user);
    }

    @Transactional
    public User applyPatchToUser(JsonPatch patch, User targetUser) throws JsonPatchException, JsonProcessingException {
        JsonNode patched = patch.apply(objectMapper.convertValue(targetUser, JsonNode.class));
        User patchedUser = objectMapper.treeToValue(patched, User.class);

        if (patchedUser == null)
            throw new IllegalArgumentException("Wrong patch request");

        patchedUser.setUserId(targetUser.getUserId());
        patchedUser.setPassword(targetUser.getPassword());
        patchedUser.setStatus(targetUser.getStatus());

        if (!patchedUser.getUsername().equals(targetUser.getUsername()))
            if (repository.existsByUsername(patchedUser.getUsername()))
                throw new UsernameUsedException("Username is already in use");
        if (!patchedUser.getEmail().equals(targetUser.getEmail())) {
            if (repository.existsByEmail(patchedUser.getEmail()))
                throw new EmailUsedException("Email is already in use");
        }
        return repository.save(patchedUser);
    }

    @Transactional
    public void deleteUserById(Long userId) {
        repository.removeUserByUserId(userId);
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

    /**
     * ПРЕДУПРЕЖДЕНИЕ: Передаваемый геттер ДОЛЖЕН ссылаться на уникальное поле.
     * Использование неуникальных полей (например, firstName) приведет к ложноположительным результатам.
     *
     * @apiNote Prefer {@link #isCurrentUserById(Long)} or {@link #isCurrentUserByUser(User)} for better safety.
     */
    @Transactional(readOnly = true)
    public <T> boolean isCurrentUser(T uniqueFeature, Function<User, T> userFieldSupplier) {
        return userFieldSupplier.apply(getCurrentUser()).equals(uniqueFeature);
    }

    @Transactional(readOnly = true)
    public boolean isCurrentUserByUser(User user) {
        return getCurrentUser().equals(user);
    }

    @Transactional(readOnly = true)
    public boolean isCurrentUserById(Long userId) {
        return getCurrentUser().getUserId().equals(userId);
    }

    /**
     * Создан для того, чтобы не делать лишние запросы к бд
     *
     * @return String username
     * @throws UserServiceException no auth exception
     */
    public String getCurrentUserName() {
        if (!securityContextWrapper.isAuthenticated())
            throw new UserServiceException("No authentication found. Current authentication is " +
                    securityContextWrapper.getAuthentication());
        return securityContextWrapper.getAuthenticatedUserInfo(UserDetails::getUsername);
    }


    public void unAuthenticate() {
        securityContextWrapper.unAuthenticate();
    }

    @Transactional
    public void changeUserStatus(UserStatus newUserStatus, String username) {
        User user = getByUsername(username);
        user.setStatus(newUserStatus);
        repository.save(user);
    }
}
