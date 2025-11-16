package io.github.platovd.alnet.service;

import io.github.platovd.alnet.dto.response.JWTAuthenticationResponse;
import io.github.platovd.alnet.dto.request.RefreshRequest;
import io.github.platovd.alnet.dto.request.SignInRequest;
import io.github.platovd.alnet.dto.request.SignUpRequest;
import io.github.platovd.alnet.entity.User;
import io.github.platovd.alnet.entity.util.Role;
import io.github.platovd.alnet.exception.InvalidRefreshToken;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthenticationService {
    private final UserService userService;
    private final JWTService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;

    public JWTAuthenticationResponse signUp(SignUpRequest signUpRequest) {
        User user = User.builder().username(signUpRequest.getName())
                .email(signUpRequest.getEmail())
                .password(passwordEncoder.encode(signUpRequest.getPassword()))
                .role(Role.ROLE_USER).build();

        userService.create(user);
        return new JWTAuthenticationResponse(jwtService.generateJWT(user), jwtService.generateJWTRefresh(user));
    }

    public JWTAuthenticationResponse signIn(SignInRequest signInRequest) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(signInRequest.getName(), signInRequest.getPassword())
        );

        UserDetails user = userService.userDetailsService().loadUserByUsername(signInRequest.getName());
        return new JWTAuthenticationResponse(jwtService.generateJWT(user), jwtService.generateJWTRefresh(user));
    }

    public JWTAuthenticationResponse refresh(RefreshRequest refreshRequest) {
        var token = refreshRequest.getRefresh();
        User user = userService.getByUsername(jwtService.extractUserName(token));
        if (!"refresh".equals(jwtService.extractTokenType(token)))
            throw new InvalidRefreshToken("Given token don't have valid type");
        if (!jwtService.isTokenValid(token, user)) throw new InvalidRefreshToken("Given refresh token isn't valid");
        return new JWTAuthenticationResponse(jwtService.generateJWT(user), jwtService.generateJWTRefresh(user));
    }
}
