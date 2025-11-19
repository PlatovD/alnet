package io.github.platovd.alnet.authentication.provider;

import io.github.platovd.alnet.authentication.token.JWTAuthToken;
import io.github.platovd.alnet.authentication.util.AuthUtil;
import io.github.platovd.alnet.entity.User;
import io.github.platovd.alnet.exception.IllegalTokenClassException;
import io.github.platovd.alnet.exception.InvalidAccessTokenException;
import io.github.platovd.alnet.exception.UnknownAuthenticationException;
import io.github.platovd.alnet.exception.UserServiceException;
import io.github.platovd.alnet.service.JWTService;
import io.github.platovd.alnet.service.UserService;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class JWTAuthenticationProvider implements AuthenticationProvider {
    private final JWTService jwtService;
    private final UserService userService;
    private final String REQUIRED_TOKEN_TYPE = "access";

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        if (!this.supports(authentication.getClass()))
            throw new IllegalTokenClassException(
                    "JWTAuthenticationProvider only supports JWTAuthToken, but got: " +
                            authentication.getClass().getName()
            );

        JWTAuthToken jwtAuth = (JWTAuthToken) authentication;
        try {
            String token = jwtAuth.getToken();
            Long userId = jwtService.extractId(token);
            User user = userService.getById(userId);

            if (!jwtService.isTypeOf(token, REQUIRED_TOKEN_TYPE) || !jwtService.isTokenValid(token, user))
                throw new InvalidAccessTokenException("Given token isn't valid for user");
            UserDetails details = AuthUtil.fromUserToUserDetails(user);
            return new JWTAuthToken(
                    token,
                    details,
                    details.getAuthorities(),
                    true
            );

        } catch (JwtException | UserServiceException exception) {
            throw new InvalidAccessTokenException("Authentication went wrong. Access denied. Cause: " + exception.getMessage());
        } catch (Exception e) {
            throw new UnknownAuthenticationException("Authentication failed. Unknown exception");
        }
    }

    @Override
    public boolean supports(Class<?> authentication) throws AuthenticationException {
        return JWTAuthToken.class.isAssignableFrom(authentication);
    }
}
