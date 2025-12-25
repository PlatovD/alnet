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

/**
 * Сервис для работы с пользователями.
 * Предоставляет основные операции для управления пользователями системы.
 *
 * @author PlatovD
 * @version 1.0
 */
@Service
@RequiredArgsConstructor
public class UserService {

    /**
     * Репозиторий для работы с пользователями.
     */
    private final UserRepository repository;

    /**
     * Кодировщик паролей.
     */
    private final PasswordEncoder passwordEncoder;

    /**
     * Объект для работы с JSON.
     */
    private final ObjectMapper objectMapper;

    /**
     * Обертка для работы с контекстом безопасности.
     */
    private final SecurityContextWrapper securityContextWrapper;

    /**
     * Создает нового пользователя.
     *
     * @param username имя пользователя
     * @param email электронная почта
     * @param password пароль
     * @return созданный пользователь
     * @throws UsernameUsedException если имя пользователя уже используется
     * @throws EmailUsedException если электронная почта уже используется
     */
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

    /**
     * Получает пользователей по списку имен пользователей.
     *
     * @param members список имен пользователей
     * @return список найденных пользователей
     */
    public List<User> getAllUsersByUsername(List<String> members) {
        Set<String> membersNormalized = members.stream().map(String::strip).filter(s -> !s.isEmpty()).collect(Collectors.toSet());
        if (membersNormalized.isEmpty()) return new ArrayList<>();
        return repository.findAllByUsernames(membersNormalized);
    }

    /**
     * Получает пользователя по имени пользователя.
     *
     * @param username имя пользователя
     * @return найденный пользователь
     * @throws UsernameNotFoundException если пользователь не найден
     */
    @Transactional(readOnly = true)
    public User getByUsername(String username) {
        return repository.findUserByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Username wasn't found"));
    }

    /**
     * Получает пользователя по идентификатору.
     *
     * @param userId идентификатор пользователя
     * @return найденный пользователь
     * @throws IdNotFoundException если пользователь не найден
     */
    @Transactional(readOnly = true)
    public User getById(Long userId) {
        return repository.findById(userId).orElseThrow(() -> new IdNotFoundException(("Id wasn't found")));
    }

    /**
     * Полностью обновляет данные пользователя.
     *
     * @param userId идентификатор пользователя
     * @param username новое имя пользователя
     * @param email новая электронная почта
     * @return обновленный пользователь
     */
    @Transactional
    public User updateFullUser(Long userId, String username, String email) {
        User user = getById(userId);
        user.setUsername(username);
        user.setEmail(email);
        return repository.save(user);
    }

    /**
     * Применяет JSON Patch к пользователю.
     *
     * @param patch JSON Patch с операциями
     * @param targetUser пользователь для обновления
     * @return обновленный пользователь
     * @throws JsonPatchException если возникает ошибка при применении патча
     * @throws JsonProcessingException если возникает ошибка при обработке JSON
     * @throws UsernameUsedException если новое имя пользователя уже используется
     * @throws EmailUsedException если новая электронная почта уже используется
     */
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

    /**
     * Удаляет пользователя по идентификатору.
     *
     * @param userId идентификатор пользователя
     */
    @Transactional
    public void deleteUserById(Long userId) {
        repository.removeUserByUserId(userId);
    }

    /**
     * Получает текущего аутентифицированного пользователя.
     *
     * @return текущий пользователь
     * @throws UserServiceException если пользователь не аутентифицирован
     */
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
     *
     * @param uniqueFeature уникальное поле для проверки
     * @param userFieldSupplier функция для получения поля из пользователя
     * @return true если поле текущего пользователя совпадает с указанным значением, иначе false
     */
    @Transactional(readOnly = true)
    public <T> boolean isCurrentUser(T uniqueFeature, Function<User, T> userFieldSupplier) {
        return userFieldSupplier.apply(getCurrentUser()).equals(uniqueFeature);
    }

    /**
     * Проверяет, является ли указанный пользователь текущим аутентифицированным пользователем.
     *
     * @param user пользователь для проверки
     * @return true если указанный пользователь является текущим, иначе false
     */
    @Transactional(readOnly = true)
    public boolean isCurrentUserByUser(User user) {
        return getCurrentUser().equals(user);
    }

    /**
     * Проверяет, является ли пользователь с указанным идентификатором текущим аутентифицированным пользователем.
     *
     * @param userId идентификатор пользователя для проверки
     * @return true если идентификатор соответствует текущему пользователю, иначе false
     */
    @Transactional(readOnly = true)
    public boolean isCurrentUserById(Long userId) {
        return getCurrentUser().getUserId().equals(userId);
    }

    /**
     * Получает имя текущего аутентифицированного пользователя.
     * Создан для того, чтобы не делать лишние запросы к бд.
     *
     * @return имя пользователя
     * @throws UserServiceException если пользователь не аутентифицирован
     */
    public String getCurrentUserName() {
        if (!securityContextWrapper.isAuthenticated())
            throw new UserServiceException("No authentication found. Current authentication is " +
                    securityContextWrapper.getAuthentication());
        return securityContextWrapper.getAuthenticatedUserInfo(UserDetails::getUsername);
    }

    /**
     * Сбрасывает аутентификацию текущего пользователя.
     */
    public void unAuthenticate() {
        securityContextWrapper.unAuthenticate();
    }

    /**
     * Изменяет статус пользователя.
     *
     * @param newUserStatus новый статус пользователя
     * @param username имя пользователя
     */
    @Transactional
    public void changeUserStatus(UserStatus newUserStatus, String username) {
        User user = getByUsername(username);
        user.setStatus(newUserStatus);
        repository.save(user);
    }
}