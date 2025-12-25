package io.github.platovd.alnet.authentication.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.platovd.alnet.authentication.contex.SecurityContextWrapper;
import io.github.platovd.alnet.authentication.entrypoint.JWTEntrypointUnauthenticated;
import io.github.platovd.alnet.authentication.token.JWTAuthToken;
import io.github.platovd.alnet.exception.authentication.InvalidAccessTokenException;
import io.github.platovd.alnet.testutil.FabricForTests;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;

import java.io.PrintWriter;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class JWTAuthenticationFilterTest {
    private final String HEADER_NAME = "Authorization";
    private final String HEADER_VALUE_VALID = "Bearer " + FabricForTests.JWT;
    private final String HEADER_VALUE_INVALID = "Bearer ";
    private final String HEADER_VALUE_NO_BEARER = "Basic dXNlcjpwYXNzd29yZA==";

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private ObjectMapper objectMapper;

    @Spy
    @InjectMocks
    private JWTEntrypointUnauthenticated jwtEntrypointUnauthenticated;

    @Spy
    private final SecurityContextWrapper securityContextWrapper = new SecurityContextWrapper();

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @Mock
    private AnonymousAuthenticationToken anonymousAuthenticationToken;

    @Mock
    private JWTAuthToken jwtAuthenticationToken;

    @Mock
    private PrintWriter printWriter;

    @Mock
    private InvalidAccessTokenException accessTokenException;

    private JWTAuthenticationFilter jwtAuthenticationFilter;

    @BeforeEach
    public void setUp() {
        jwtAuthenticationFilter =
                new JWTAuthenticationFilter(securityContextWrapper, authenticationManager, jwtEntrypointUnauthenticated);
        jwtAuthenticationFilter = Mockito.spy(jwtAuthenticationFilter);
    }

    @SneakyThrows
    @Test
    public void doFilterInternalCorrectTest() {
        doNothing().when(jwtAuthenticationFilter).doFilter(request, response, filterChain);
        when(request.getHeader(HEADER_NAME)).thenReturn(HEADER_VALUE_VALID);
        when(authenticationManager.authenticate(any(Authentication.class))).then(invocation -> {
            JWTAuthToken jwt = new JWTAuthToken(FabricForTests.JWT);
            jwt.setAuthenticated(true);
            return jwt;
        });
        securityContextWrapper.setAnonymousAuthKey(FabricForTests.ANONYMOUS_KEY);
        securityContextWrapper.setAuthentication(anonymousAuthenticationToken);

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);
        assertThat(securityContextWrapper.isAuthenticated()).isTrue();
    }

    @SneakyThrows
    @Test
    public void doFilterInternalAlreadyAuthenticatedTest() {
        doNothing().when(jwtAuthenticationFilter).doFilter(request, response, filterChain);
        securityContextWrapper.setAnonymousAuthKey(FabricForTests.ANONYMOUS_KEY);
        when(request.getHeader(HEADER_NAME)).thenReturn(HEADER_VALUE_VALID);
        when(jwtAuthenticationToken.isAuthenticated()).thenReturn(true);
        securityContextWrapper.setAuthentication(jwtAuthenticationToken);

        securityContextWrapper.setAuthentication(jwtAuthenticationToken);
        assertThatNoException().isThrownBy(() -> jwtAuthenticationFilter.doFilterInternal(request, response, filterChain));
    }

    @Test
    @SneakyThrows
    public void doFilterInternalNoBearerInHeaderTest() {
        doNothing().when(jwtAuthenticationFilter).doFilter(request, response, filterChain);
        securityContextWrapper.setAnonymousAuthKey(FabricForTests.ANONYMOUS_KEY);
        when(request.getHeader(HEADER_NAME)).thenReturn(HEADER_VALUE_NO_BEARER);
        securityContextWrapper.setAuthentication(jwtAuthenticationToken);

        securityContextWrapper.setAuthentication(jwtAuthenticationToken);
        assertThatNoException().isThrownBy(() -> jwtAuthenticationFilter.doFilterInternal(request, response, filterChain));
    }

    @SneakyThrows
    @Test
    public void doFilterInternalIncorrectTokenTest() {
        securityContextWrapper.setAnonymousAuthKey(FabricForTests.ANONYMOUS_KEY);
        when(request.getHeader(HEADER_NAME)).thenReturn(HEADER_VALUE_VALID);
        securityContextWrapper.setAuthentication(anonymousAuthenticationToken);
        when(accessTokenException.getMessage()).thenReturn("Error message");
        when(authenticationManager.authenticate(any(Authentication.class))).thenThrow(accessTokenException);
        doNothing().when(response).setStatus(any(Integer.class));
        doNothing().when(response).setContentType(any(String.class));
        doNothing().when(response).setCharacterEncoding(any(String.class));
        when(response.getWriter()).thenReturn(printWriter);
        when(objectMapper.writeValueAsString(any(Map.class))).thenReturn("");
        doNothing().when(printWriter).print(any(String.class));


        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);
        verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        verify(response).setContentType("application/json");
        verify(objectMapper).writeValueAsString(any(Map.class));
        assertThat(securityContextWrapper.getAuthentication())
                .isInstanceOf(AnonymousAuthenticationToken.class);
    }

    @Test
    @SneakyThrows
    public void doFilterInternalEmptyTokenTest() {
        securityContextWrapper.setAnonymousAuthKey(FabricForTests.ANONYMOUS_KEY);
        when(request.getHeader(HEADER_NAME)).thenReturn(HEADER_VALUE_INVALID);
        securityContextWrapper.setAuthentication(anonymousAuthenticationToken);
        doNothing().when(response).setStatus(any(Integer.class));
        doNothing().when(response).setContentType(any(String.class));
        doNothing().when(response).setCharacterEncoding(any(String.class));
        when(response.getWriter()).thenReturn(printWriter);
        when(objectMapper.writeValueAsString(any(Map.class))).thenReturn("");
        doNothing().when(printWriter).print(any(String.class));


        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);
        verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        verify(response).setContentType("application/json");
        verify(objectMapper).writeValueAsString(any(Map.class));
        assertThat(securityContextWrapper.getAuthentication())
                .isInstanceOf(AnonymousAuthenticationToken.class);
    }
}
