package io.github.platovd.alnet.service;

import io.github.platovd.alnet.authentication.contex.SecurityContextWrapper;
import io.github.platovd.alnet.dto.request.RefreshRequest;
import io.github.platovd.alnet.dto.request.SignInRequest;
import io.github.platovd.alnet.dto.request.SignUpRequest;
import io.github.platovd.alnet.dto.response.JWTAuthenticationResponse;
import io.github.platovd.alnet.entity.User;
import io.github.platovd.alnet.exception.UnknownAuthenticationException;
import io.github.platovd.alnet.exception.UserServiceException;
import io.github.platovd.alnet.testutil.FabricForTests;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthenticationServiceTest {
    private SignUpRequest signUpRequest;
    private SignInRequest signInRequest;
    private RefreshRequest refreshRequest;

    @Mock
    private UserService userService;

    @Mock
    private SecurityContextWrapper securityContextWrapper;

    private User testUser;

    @Mock
    private JWTService jwtService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    @InjectMocks
    private AuthenticationService authenticationService;

    @BeforeEach
    public void setUp() {
        signUpRequest = FabricForTests.signUpRequest();
        signInRequest = FabricForTests.signInRequest();
        refreshRequest = FabricForTests.refreshRequest();
        testUser = FabricForTests.testUser();
    }

    @Test
    public void signUpSuccessTest() {
        doNothing().when(userService).create(any(User.class));
        when(passwordEncoder.encode(any(String.class))).thenReturn(FabricForTests.PASSWORD);
        when(jwtService.generateJWTAccess(any(User.class))).thenReturn("");
        when(jwtService.generateJWTRefresh(any(User.class))).thenReturn("");

        assertThat(authenticationService.signUp(signUpRequest)).isInstanceOf(JWTAuthenticationResponse.class);
        verify(userService, times(1)).create(any(User.class));
    }

    @Test
    public void signUpUnsuccessTest() {
        doThrow(UserServiceException.class).when(userService).create(any(User.class));
        assertThatThrownBy(() -> authenticationService.signUp(signUpRequest)).isInstanceOf(UserServiceException.class);
    }

    @Test
    public void signInSuccessTest() {
        when(securityContextWrapper.isAuthenticated()).thenReturn(false);
        when(authenticationManager.authenticate(any(Authentication.class))).thenReturn(null);
        when(userService.getByUsername(testUser.getUsername())).thenReturn(testUser);

        assertThat(authenticationService.signIn(signInRequest)).isInstanceOf(JWTAuthenticationResponse.class);
        verify(userService, times(1)).getByUsername(testUser.getUsername());
    }

    @Test
    public void signInUnsuccessAlreadyAuthenticatedTest() {
        when(securityContextWrapper.isAuthenticated()).thenReturn(true);
        assertThatThrownBy(() -> authenticationService.signIn(signInRequest)).isInstanceOf(AuthenticationException.class);
    }

    @Test
    public void signInUnsuccessNoAuthenticationTest() {
        when(securityContextWrapper.isAuthenticated()).thenReturn(false);
        when(authenticationManager.authenticate(any(Authentication.class))).thenThrow(UnknownAuthenticationException.class);

        assertThatThrownBy(() -> authenticationService.signIn(signInRequest)).isInstanceOf(AuthenticationException.class);
        verify(securityContextWrapper, times(1)).unAuthenticate();
    }

    @Test
    public void refreshSuccessTest() {
        when(userService.getById(any(Long.class))).thenReturn(testUser);
        when(jwtService.extractTokenType(any(String.class))).thenReturn("refresh");
        when(jwtService.isTokenValid(any(String.class), eq(testUser))).thenReturn(true);

        assertThat(authenticationService.refresh(refreshRequest)).isInstanceOf(JWTAuthenticationResponse.class);
    }
}