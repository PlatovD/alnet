package io.github.platovd.alnet.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.fge.jsonpatch.JsonPatch;
import com.github.fge.jsonpatch.JsonPatchException;
import io.github.platovd.alnet.dto.user.UserDTO;
import io.github.platovd.alnet.entity.User;
import io.github.platovd.alnet.service.atomic.UserService;
import io.github.platovd.alnet.service.orchestration.UserFacade;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.web.bind.annotation.*;

/**
 * Контроллер для управления пользователями.
 * Предоставляет endpoints для получения, обновления и удаления пользовательских данных.
 *
 * @author PlatovD
 * @version 1.0
 */
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "Управление пользователями")
public class UserController {

    /**
     * Фасад для работы с пользователями.
     */
    private final UserFacade userFacade;

    /**
     * Получает данные пользователя по его идентификатору.
     *
     * @param userId идентификатор пользователя
     * @return данные пользователя в формате DTO
     */
    @Operation(description = "Получение данных пользователя")
    @GetMapping("/{userId}")
    public UserDTO getUser(@PathVariable Long userId) {
        return userFacade.getUser(userId);
    }

    /**
     * Полностью обновляет данные пользователя.
     * Требуется совпадение идентификатора пользователя с текущим аутентифицированным пользователем.
     *
     * @param userId идентификатор пользователя
     * @param putRequest новые данные пользователя
     * @return обновленные данные пользователя в формате DTO
     */
    @PreAuthorize("@userSecurity.isCurrentUser(#userId)")
    @Operation(description = "Обновление пользователя")
    @PutMapping("/{userId}")
    public UserDTO updateUser(@PathVariable Long userId, @Valid @RequestBody UserDTO putRequest) {
        return userFacade.updateFullUser(userId, putRequest);
    }

    /**
     * Частично обновляет данные пользователя с использованием JSON Patch.
     * Требуется совпадение идентификатора пользователя с текущим аутентифицированным пользователем.
     *
     * @param userId идентификатор пользователя
     * @param jsonPatch объект JSON Patch с операциями обновления
     * @return обновленные данные пользователя в формате DTO
     * @throws JsonPatchException если возникает ошибка при применении JSON Patch
     * @throws JsonProcessingException если возникает ошибка при обработке JSON
     */
    @PreAuthorize("@userSecurity.isCurrentUser(#userId)")
    @Operation(description = "Обновление части данных пользователя")
    @PatchMapping(path = "/{userId}", consumes = "application/json-patch+json")
    public UserDTO patchUser(@PathVariable Long userId, @RequestBody JsonPatch jsonPatch) throws JsonPatchException, JsonProcessingException {
        return userFacade.patchUserById(userId, jsonPatch);
    }

    /**
     * Удаляет пользователя по его идентификатору.
     * Требуется совпадение идентификатора пользователя с текущим аутентифицированным пользователем.
     *
     * @param userId идентификатор пользователя для удаления
     * @return ответ с HTTP статусом 204 No Content
     */
    @PreAuthorize("@userSecurity.isCurrentUser(#userId)")
    @Operation(description = "Удаление пользователя")
    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long userId) {
        userFacade.deleteUser(userId);
        return ResponseEntity.status(204).build();
    }
}
