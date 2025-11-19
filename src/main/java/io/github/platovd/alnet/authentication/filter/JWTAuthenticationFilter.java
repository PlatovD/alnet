package io.github.platovd.alnet.authentication.filter;

import io.github.platovd.alnet.authentication.contex.SecurityContextWrapper;
import io.github.platovd.alnet.authentication.token.JWTAuthToken;
import io.micrometer.common.lang.NonNull;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@RequiredArgsConstructor
@Setter
public class JWTAuthenticationFilter extends OncePerRequestFilter {
    public static final String BEARER_PREFIX = "Bearer ";
    public static final String HEADER_NAME = "Authorization";
    private final SecurityContextWrapper securityContextWrapper;
    private AuthenticationManager authManager;
    private final AuthenticationEntryPoint authenticationEntryPoint;
    private final boolean ignoreFailure = false;


    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain filterChain) throws ServletException, IOException {
        try {
            if (!checkBearer(request)) {
                doFilter(request, response, filterChain);
                return;
            }

            var jwt = request.getHeader(HEADER_NAME).substring(BEARER_PREFIX.length());
            if (jwt.isEmpty() || isAuthenticationRequired()) {
                doFilter(request, response, filterChain);
                return;
            }

            Authentication token = new JWTAuthToken(jwt);
            Authentication authentication = authManager.authenticate(token);
            securityContextWrapper.setAuthentication(authentication);
            onSuccessfulAuthentication(request, response, authentication);
        } catch (AuthenticationException e) {
            securityContextWrapper.unAuthenticate();
            onUnsuccessfulAuthentication(request, response, e);
            if (ignoreFailure) {
                doFilter(request, response, filterChain);
            } else {
                authenticationEntryPoint.commence(request, response, e);
            }
            return;
        }
        doFilter(request, response, filterChain);
    }

    protected boolean isAuthenticationRequired() {
        return securityContextWrapper.getAuthentication() != null &&
                securityContextWrapper.isAuthenticated() &&
                !(securityContextWrapper.getAuthentication() instanceof AnonymousAuthenticationToken);
    }

    protected boolean checkBearer(@NonNull HttpServletRequest request) {
        String header = request.getHeader(HEADER_NAME);
        return !StringUtils.isEmpty(header) && header.startsWith(BEARER_PREFIX);
    }

    protected void onSuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response, Authentication
            authResult) throws IOException {
    }

    protected void onUnsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response, AuthenticationException
            failed) throws IOException {
    }
}
