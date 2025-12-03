package io.github.platovd.alnet.service;

import io.github.platovd.alnet.authentication.contex.SecurityContextWrapper;
import io.github.platovd.alnet.authentication.token.JWTAuthToken;

import io.github.platovd.alnet.dto.authentication.request.RefreshRequest;
import io.github.platovd.alnet.dto.authentication.request.SignInRequest;
import io.github.platovd.alnet.dto.authentication.request.SignUpRequest;
import io.github.platovd.alnet.dto.authentication.response.JWTAuthenticationResponse;
import io.github.platovd.alnet.entity.User;
import io.github.platovd.alnet.exception.authentication.AlreadyAuthenticatedException;
import io.github.platovd.alnet.exception.authentication.InvalidRefreshTokenException;
import io.github.platovd.alnet.exception.user.UserServiceException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthenticationService {
    private final UserService userService;
    private final JWTService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final SecurityContextWrapper securityContextWrapper;

    @Transactional
    public JWTAuthenticationResponse signUp(SignUpRequest signUpRequest) {
        User user = User.builder().username(signUpRequest.getUsername())
                .email(signUpRequest.getEmail())
                .password(passwordEncoder.encode(signUpRequest.getPassword()))
                .build();

        userService.create(user);
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
    public User getCurrentUser() {
        if (!securityContextWrapper.isAuthenticated())
            throw new UserServiceException("No authentication found. Current authentication is " +
                    securityContextWrapper.getAuthentication());
        Authentication authentication = securityContextWrapper.getAuthentication();
        if (authentication instanceof JWTAuthToken jwtAuthToken) {
            return userService.getById(jwtAuthToken.getId());
        }
        return userService.getByUsername(authentication.getName());
    }

    @Transactional(readOnly = true)
    public boolean isCurrentUser(User user) {
        return getCurrentUser().getUserId().equals(user.getUserId());
    }

    public void unAuthenticate() {
        securityContextWrapper.unAuthenticate();
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
}
