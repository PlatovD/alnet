package io.github.platovd.alnet.service;

import io.github.platovd.alnet.authentication.contex.SecurityContextWrapper;
import io.github.platovd.alnet.dto.response.JWTAuthenticationResponse;
import io.github.platovd.alnet.dto.request.RefreshRequest;
import io.github.platovd.alnet.dto.request.SignInRequest;
import io.github.platovd.alnet.dto.request.SignUpRequest;
import io.github.platovd.alnet.entity.User;
import io.github.platovd.alnet.exception.AlreadyAuthenticatedException;
import io.github.platovd.alnet.exception.InvalidRefreshTokenException;
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
        User user = User.builder().username(signUpRequest.getName())
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
                    new UsernamePasswordAuthenticationToken(signInRequest.getName(), signInRequest.getPassword())
            );

            User user = userService.getByUsername(signInRequest.getName());
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
}
