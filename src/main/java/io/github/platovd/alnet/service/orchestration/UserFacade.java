package io.github.platovd.alnet.service.orchestration;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.fge.jsonpatch.JsonPatch;
import com.github.fge.jsonpatch.JsonPatchException;
import io.github.platovd.alnet.authentication.contex.SecurityContextWrapper;
import io.github.platovd.alnet.dto.authentication.request.RefreshRequest;
import io.github.platovd.alnet.dto.authentication.request.SignInRequest;
import io.github.platovd.alnet.dto.authentication.request.SignUpRequest;
import io.github.platovd.alnet.dto.authentication.response.JWTAuthenticationResponse;
import io.github.platovd.alnet.dto.user.UserDTO;
import io.github.platovd.alnet.entity.User;
import io.github.platovd.alnet.exception.authentication.AlreadyAuthenticatedException;
import io.github.platovd.alnet.exception.authentication.InvalidRefreshTokenException;
import io.github.platovd.alnet.exception.base.WrongDataException;
import io.github.platovd.alnet.mapper.UserMapper;
import io.github.platovd.alnet.service.atomic.JWTService;
import io.github.platovd.alnet.service.atomic.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

/**
 * Фасад для работы с пользователями и аутентификацией.
 * Координирует работу нескольких сервисов для выполнения операций с пользователями.
 *
 * @author PlatovD
 * @version 1.0
 */
@Service
@RequiredArgsConstructor
public class UserFacade {

    /**
     * Сервис для работы с пользователями.
     */
    private final UserService userService;

    /**
     * Сервис для работы с JWT токенами.
     */
    private final JWTService jwtService;

    /**
     * Менеджер аутентификации.
     */
    private final AuthenticationManager authenticationManager;

    /**
     * Обертка для работы с контекстом безопасности.
     */
    private final SecurityContextWrapper securityContextWrapper;

    /**
     * Маппер для преобразования пользователей.
     */
    private final UserMapper userMapper;

    /**
     * Регистрирует нового пользователя в системе.
     *
     * @param signUpRequest запрос с данными для регистрации
     * @return JWT токены для нового пользователя
     */
    @Transactional
    public JWTAuthenticationResponse signUp(SignUpRequest signUpRequest) {
        User user = userService.create(signUpRequest.getUsername(), signUpRequest.getEmail(),
                signUpRequest.getPassword());
        return new JWTAuthenticationResponse(jwtService.generateJWTAccess(user), jwtService.generateJWTRefresh(user));
    }

    /**
     * Аутентифицирует пользователя в системе.
     *
     * @param signInRequest запрос с данными для аутентификации
     * @return JWT токены для аутентифицированного пользователя
     * @throws AlreadyAuthenticatedException если пользователь уже аутентифицирован
     * @throws AuthenticationException если аутентификация не удалась
     */
    @Transactional(readOnly = true)
    public JWTAuthenticationResponse signIn(SignInRequest signInRequest) {
        if (securityContextWrapper.isAuthenticated())
            throw new AlreadyAuthenticatedException("Logout first from " +
                    securityContextWrapper.getAuthenticatedUserInfo(UserDetails::getUsername));
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(signInRequest.getUsername(), signInRequest.getPassword())
            );

            User user = userService.getByUsername(signInRequest.getUsername());
            return new JWTAuthenticationResponse(jwtService.generateJWTAccess(user), jwtService.generateJWTRefresh(user));
        } catch (AuthenticationException e) {
            securityContextWrapper.unAuthenticate();
            throw e;
        }
    }

    /**
     * Обновляет JWT токены с использованием refresh токена.
     *
     * @param refreshRequest запрос с refresh токеном
     * @return новые JWT токены
     * @throws InvalidRefreshTokenException если refresh токен невалиден
     */
    @Transactional(readOnly = true)
    public JWTAuthenticationResponse refresh(RefreshRequest refreshRequest) {
        var token = refreshRequest.getRefresh();
        User user = userService.getById(jwtService.extractId(token));
        if (!"refresh".equals(jwtService.extractTokenType(token)))
            throw new InvalidRefreshTokenException("Given token don't have valid type");
        if (!jwtService.isTokenValid(token, user))
            throw new InvalidRefreshTokenException("Given refresh token isn't valid");
        return new JWTAuthenticationResponse(jwtService.generateJWTAccess(user), jwtService.generateJWTRefresh(user));
    }

    /**
     * Получает данные пользователя.
     * Скрывает email если запрашиваемый пользователь не является текущим аутентифицированным пользователем.
     *
     * @param userId идентификатор пользователя
     * @return данные пользователя
     */
    @Transactional(readOnly = true)
    public UserDTO getUser(Long userId) {
        User user = userService.getById(userId);
        UserDTO response = userMapper.toDTO(user);
        if (!userService.isCurrentUser(user.getUserId(), User::getUserId))
            response.setEmail("");
        return response;
    }

    /**
     * Полностью обновляет данные пользователя.
     *
     * @param userId идентификатор пользователя из URL
     * @param putRequest новые данные пользователя
     * @return обновленные данные пользователя
     * @throws WrongDataException если идентификатор в URL не совпадает с идентификатором в запросе
     */
    @Transactional
    public UserDTO updateFullUser(Long userId, UserDTO putRequest) {
        if (!Objects.equals(userId, putRequest.getUserId()))
            throw new WrongDataException("Url param and request body have conflict information");
        User updatedUser = userService.updateFullUser(
                userId, putRequest.getUsername(), putRequest.getEmail()
        );
        return userMapper.toDTO(updatedUser);
    }

    /**
     * Частично обновляет данные пользователя с использованием JSON Patch.
     *
     * @param userId идентификатор пользователя
     * @param jsonPatch JSON Patch с операциями
     * @return обновленные данные пользователя
     * @throws JsonPatchException если возникает ошибка при применении патча
     * @throws JsonProcessingException если возникает ошибка при обработке JSON
     */
    public UserDTO patchUserById(Long userId, JsonPatch jsonPatch) throws JsonPatchException, JsonProcessingException {
        User user = userService.getById(userId);
        User patchedUser = userService.applyPatchToUser(jsonPatch, user);
        return userMapper.toDTO(patchedUser);
    }

    /**
     * Удаляет пользователя.
     *
     * @param userId идентификатор пользователя
     */
    public void deleteUser(Long userId) {
        userService.unAuthenticate();
        userService.deleteUserById(userId);
    }
}
