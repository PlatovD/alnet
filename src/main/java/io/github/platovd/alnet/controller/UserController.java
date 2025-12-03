package io.github.platovd.alnet.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.fge.jsonpatch.JsonPatch;
import com.github.fge.jsonpatch.JsonPatchException;
import io.github.platovd.alnet.dto.user.request.UserPutRequest;
import io.github.platovd.alnet.dto.user.response.UserDTO;
import io.github.platovd.alnet.entity.User;
import io.github.platovd.alnet.service.AuthenticationService;
import io.github.platovd.alnet.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "Управление пользователями")
public class UserController {
    private final AuthenticationService authenticationService;
    private final UserService userService;

    @Operation(description = "Получение данных пользователя")
    @GetMapping("/{userId}")
    public UserDTO getUser(@PathVariable Long userId) {
        User user = userService.getById(userId);
        UserDTO response = UserDTO.builder().username(user.getUsername()).build();
        if (authenticationService.isCurrentUser(user))
            response.setEmail(user.getEmail());
        return response;
    }

    @Operation(description = "Обновление пользователя")
    @PutMapping("/{userId}")
    public UserDTO updateUser(@PathVariable Long userId, @Valid @RequestBody UserPutRequest putRequest) {
        User user = userService.getById(userId);
        if (!authenticationService.isCurrentUser(user))
            throw new AuthorizationDeniedException("Can't edit profile of another user");
        User updatedUser = userService.updateFullUser(
                userId, putRequest.getUsername(), putRequest.getEmail()
        );
        return UserDTO.builder().username(updatedUser.getUsername()).email(updatedUser.getEmail()).build();
    }

    @Operation(description = "Обновление части данных пользователя")
    @PatchMapping(path = "/{userId}", consumes = "application/json-patch+json")
    public UserDTO patchUser(@PathVariable Long userId, @RequestBody JsonPatch jsonPatch) throws JsonPatchException, JsonProcessingException {
        User user = userService.getById(userId);
        if (!authenticationService.isCurrentUser(user))
            throw new AuthorizationDeniedException("Can't edit profile of another user");
        User patchedUser = userService.applyPatchToUser(jsonPatch, user);
        return UserDTO.builder().username(patchedUser.getUsername()).email(patchedUser.getEmail()).build();
    }

    @Operation(description = "Удаление пользователя")
    @DeleteMapping("/{userId}")
    public ResponseEntity<String> deleteUser(@PathVariable Long userId) {
        User user = userService.getById(userId);
        if (!authenticationService.isCurrentUser(user))
            throw new AuthorizationDeniedException("Can't delete profile of another user");
        authenticationService.unAuthenticate();
        userService.deleteUserById(userId);
        return ResponseEntity.ok("Deleted");
    }
}
