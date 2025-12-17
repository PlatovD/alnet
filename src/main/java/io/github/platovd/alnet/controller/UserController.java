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

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "Управление пользователями")
public class UserController {
    private final UserFacade userFacade;

    @Operation(description = "Получение данных пользователя")
    @GetMapping("/{userId}")
    public UserDTO getUser(@PathVariable Long userId) {
        return userFacade.getUser(userId);
    }

    @PreAuthorize("@userSecurity.isCurrentUser(#userId)")
    @Operation(description = "Обновление пользователя")
    @PutMapping("/{userId}")
    public UserDTO updateUser(@PathVariable Long userId, @Valid @RequestBody UserDTO putRequest) {
        return userFacade.updateFullUser(userId, putRequest);
    }

    @PreAuthorize("@userSecurity.isCurrentUser(#userId)")
    @Operation(description = "Обновление части данных пользователя")
    @PatchMapping(path = "/{userId}", consumes = "application/json-patch+json")
    public UserDTO patchUser(@PathVariable Long userId, @RequestBody JsonPatch jsonPatch) throws JsonPatchException, JsonProcessingException {
        return userFacade.patchUserById(userId, jsonPatch);
    }

    @PreAuthorize("@userSecurity.isCurrentUser(#userId)")
    @Operation(description = "Удаление пользователя")
    @DeleteMapping("/{userId}")
    public ResponseEntity<String> deleteUser(@PathVariable Long userId) {
        return userFacade.deleteUser(userId);
    }
}
