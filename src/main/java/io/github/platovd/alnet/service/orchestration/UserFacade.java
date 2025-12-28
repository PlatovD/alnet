package io.github.platovd.alnet.service.orchestration;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.fge.jsonpatch.JsonPatch;
import com.github.fge.jsonpatch.JsonPatchException;
import io.github.platovd.alnet.wrapper.SecurityContextWrapper;
import io.github.platovd.alnet.dto.authentication.request.RefreshRequest;
import io.github.platovd.alnet.dto.authentication.request.SignInRequest;
import io.github.platovd.alnet.dto.authentication.request.SignUpRequest;
import io.github.platovd.alnet.dto.authentication.response.JWTAuthenticationResponse;
import io.github.platovd.alnet.dto.user.UserDTO;
import io.github.platovd.alnet.entity.User;
import io.github.platovd.alnet.exception.entity.authentication.AlreadyAuthenticatedException;
import io.github.platovd.alnet.exception.entity.authentication.InvalidRefreshTokenException;
import io.github.platovd.alnet.exception.general.WrongDataException;
import io.github.platovd.alnet.mapper.UserMapper;
import io.github.platovd.alnet.service.atomic.JWTService;
import io.github.platovd.alnet.service.atomic.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Service
@RequiredArgsConstructor
public class UserFacade {
    private final UserService userService;
    private final JWTService jwtService;
    private final AuthenticationManager authenticationManager;
    private final SecurityContextWrapper securityContextWrapper;
    private final UserMapper userMapper;

    @Transactional
    public JWTAuthenticationResponse signUp(SignUpRequest signUpRequest) {
        User user = userService.create(signUpRequest.getUsername(), signUpRequest.getEmail(),
                signUpRequest.getPassword());
        return new JWTAuthenticationResponse(jwtService.generateJWTAccess(user), jwtService.generateJWTRefresh(user));
    }

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

    @Transactional(readOnly = true)
    public UserDTO getUser(Long userId) {
        User user = userService.getById(userId);
        UserDTO response = userMapper.toDTO(user);
        if (!userService.isCurrentUser(user.getUserId(), User::getUserId))
            response.setEmail("");
        return response;
    }

    @Transactional
    public UserDTO updateFullUser(Long userId, UserDTO putRequest) {
        if (!Objects.equals(userId, putRequest.getUserId()))
            throw new WrongDataException("Url param and request body have conflict information");
        User updatedUser = userService.updateFullUser(
                userId, putRequest.getUsername(), putRequest.getEmail()
        );
        return userMapper.toDTO(updatedUser);
    }

    public UserDTO patchUserById(Long userId, JsonPatch jsonPatch) throws JsonPatchException, JsonProcessingException {
        User user = userService.getById(userId);
        User patchedUser = userService.applyPatchToUser(jsonPatch, user);
        return userMapper.toDTO(patchedUser);
    }

    public void deleteUser(Long userId) {
        userService.unAuthenticate();
        userService.deleteUserById(userId);
    }
}
