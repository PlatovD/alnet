package io.github.platovd.alnet.authentication.provider;

import io.github.platovd.alnet.authentication.token.JWTAuthToken;
import io.github.platovd.alnet.entity.Role;
import io.github.platovd.alnet.entity.User;
import io.github.platovd.alnet.exception.UserServiceException;
import io.github.platovd.alnet.service.JWTService;
import io.github.platovd.alnet.service.UserService;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class JWTAuthenticationProviderTest {
    private final String JWT = "jwt";
    private final Long USER_ID = 1L;
    private final String USERNAME = "Test";
    private final String ROLE = "USER";
    private User testUser;
    private JWTAuthToken jwtAuthToken;

    @Mock
    private UserService userService;

    @Mock
    private JWTService jwtService;

    @InjectMocks
    private JWTAuthenticationProvider authenticationProvider;


    @BeforeEach
    public void setUp() {
        jwtAuthToken = new JWTAuthToken(JWT);
        testUser = User.builder().id(USER_ID).email("test@gmail.com").username(USERNAME).password("qwerty").role(
                List.of(Role.builder().name(ROLE).build())).build();
    }

    @Test
    public void validJWTTest() {
        when(jwtService.extractId(JWT)).thenReturn(USER_ID);
        when(userService.getById(USER_ID)).thenReturn(testUser);
        when(jwtService.isTypeOf(JWT, "access")).thenReturn(true);
        when(jwtService.isTokenValid(JWT, testUser)).thenReturn(true);

        Authentication res = authenticationProvider.authenticate(jwtAuthToken);

        assertThat(res.isAuthenticated()).isTrue();
        assertThat(res.getName()).isEqualTo(testUser.getUsername());
        assertThat(res.getAuthorities())
                .extracting(GrantedAuthority::getAuthority)
                .contains("ROLE_USER");
    }

    @Test
    public void invalidJWTKeyTest() {
        when(jwtService.extractId(JWT)).thenThrow(JwtException.class);
        assertThatThrownBy(() -> authenticationProvider.authenticate(jwtAuthToken))
                .isInstanceOf(AuthenticationException.class);
        assertThat(jwtAuthToken.isAuthenticated()).isFalse();
    }

    @Test
    public void invalidJWTUserTest() {
        when(jwtService.extractId(JWT)).thenReturn(USER_ID);
        when(userService.getById(USER_ID)).thenThrow(UserServiceException.class);
        assertThatThrownBy(() -> authenticationProvider.authenticate(jwtAuthToken))
                .isInstanceOf(AuthenticationException.class);
        assertThat(jwtAuthToken.isAuthenticated()).isFalse();
    }

    @Test
    public void expiredJWTTest() {
        when(jwtService.extractId(JWT)).thenReturn(USER_ID);
        when(userService.getById(USER_ID)).thenReturn(testUser);
        when(jwtService.isTypeOf(JWT, "access")).thenReturn(true);
        when(jwtService.isTokenValid(JWT, testUser)).thenThrow(ExpiredJwtException.class);

        assertThatThrownBy(() -> authenticationProvider.authenticate(jwtAuthToken))
                .isInstanceOf(AuthenticationException.class);
        assertThat(jwtAuthToken.isAuthenticated()).isFalse();
    }
}
