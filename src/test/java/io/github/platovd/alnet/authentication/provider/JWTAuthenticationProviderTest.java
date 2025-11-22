package io.github.platovd.alnet.authentication.provider;

import io.github.platovd.alnet.authentication.token.JWTAuthToken;
import io.github.platovd.alnet.entity.User;
import io.github.platovd.alnet.exception.UserServiceException;
import io.github.platovd.alnet.service.JWTService;
import io.github.platovd.alnet.service.UserService;
import io.github.platovd.alnet.testutil.FabricForTests;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class JWTAuthenticationProviderTest {
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
        jwtAuthToken = new JWTAuthToken(FabricForTests.JWT);
        testUser = FabricForTests.testUser();
    }

    @Test
    public void validJWTTest() {
        when(jwtService.extractId(FabricForTests.JWT)).thenReturn(FabricForTests.USER_ID);
        when(userService.getById(FabricForTests.USER_ID)).thenReturn(testUser);
        when(jwtService.isTypeOf(FabricForTests.JWT, "access")).thenReturn(true);
        when(jwtService.isTokenValid(FabricForTests.JWT, testUser)).thenReturn(true);

        Authentication res = authenticationProvider.authenticate(jwtAuthToken);

        assertThat(res.isAuthenticated()).isTrue();
        assertThat(res.getName()).isEqualTo(testUser.getUsername());
        assertThat(res.getAuthorities())
                .extracting(GrantedAuthority::getAuthority)
                .contains("ROLE_" + FabricForTests.ROLE);
    }

    @Test
    public void invalidJWTKeyTest() {
        when(jwtService.extractId(FabricForTests.JWT)).thenThrow(JwtException.class);
        assertThatThrownBy(() -> authenticationProvider.authenticate(jwtAuthToken))
                .isInstanceOf(AuthenticationException.class);
        assertThat(jwtAuthToken.isAuthenticated()).isFalse();
    }

    @Test
    public void invalidJWTUserTest() {
        when(jwtService.extractId(FabricForTests.JWT)).thenReturn(FabricForTests.USER_ID);
        when(userService.getById(FabricForTests.USER_ID)).thenThrow(UserServiceException.class);
        assertThatThrownBy(() -> authenticationProvider.authenticate(jwtAuthToken))
                .isInstanceOf(AuthenticationException.class);
        assertThat(jwtAuthToken.isAuthenticated()).isFalse();
    }

    @Test
    public void expiredJWTTest() {
        when(jwtService.extractId(FabricForTests.JWT)).thenReturn(FabricForTests.USER_ID);
        when(userService.getById(FabricForTests.USER_ID)).thenReturn(testUser);
        when(jwtService.isTypeOf(FabricForTests.JWT, "access")).thenReturn(true);
        when(jwtService.isTokenValid(FabricForTests.JWT, testUser)).thenThrow(ExpiredJwtException.class);

        assertThatThrownBy(() -> authenticationProvider.authenticate(jwtAuthToken))
                .isInstanceOf(AuthenticationException.class);
        assertThat(jwtAuthToken.isAuthenticated()).isFalse();
    }
}
