package io.github.platovd.alnet.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.fge.jsonpatch.JsonPatch;
import com.github.fge.jsonpatch.JsonPatchException;
import io.github.platovd.alnet.dto.user.request.UserPutRequest;
import io.github.platovd.alnet.dto.user.response.UserDTO;
import io.github.platovd.alnet.entity.User;
import io.github.platovd.alnet.service.AuthenticationService;
import io.github.platovd.alnet.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    private final AuthenticationService authenticationService;
    private final UserService userService;

    @GetMapping("/{userId}")
    public UserDTO getUser(@PathVariable Long userId) {
        User user = userService.getById(userId);
        return new UserDTO(user);
    }

    @PutMapping("/{userId}")
    public UserDTO updateUser(@PathVariable Long userId, @RequestBody @Valid UserPutRequest putRequest) {
        User user = userService.getById(userId);
        if (!authenticationService.isCurrentUser(user))
            throw new AuthorizationDeniedException("Can't edit profile of another user");
        user = userService.updateFullUser(
                userId, putRequest.getUsername(), putRequest.getEmail()
        );
        return new UserDTO(user);
    }

    @PatchMapping(path = "/{userId}", consumes = "application/json-patch+json")
    public UserDTO patchUser(@PathVariable Long userId, @RequestBody JsonPatch jsonPatch) throws JsonPatchException, JsonProcessingException {
        User user = userService.getById(userId);
        if (!authenticationService.isCurrentUser(user))
            throw new AuthorizationDeniedException("Can't edit profile of another user");
        return new UserDTO(userService.applyPatchToUser(jsonPatch, user));
    }

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
