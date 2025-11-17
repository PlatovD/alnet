package io.github.platovd.alnet.authentication.filter;

import io.github.platovd.alnet.authentication.contex.SecurityContextWrapper;
import io.github.platovd.alnet.authentication.token.JWTAuthToken;
import io.micrometer.common.lang.NonNull;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JWTAuthenticationFilter extends OncePerRequestFilter {
    public static final String BEARER_PREFIX = "Bearer ";
    public static final String HEADER_NAME = "Authorization";
    private final SecurityContextWrapper securityContextWrapper;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain filterChain) throws ServletException, IOException {
        if (!checkBearer(request)) return;
        var jwt = request.getHeader(HEADER_NAME).substring(BEARER_PREFIX.length());
        if (!jwt.isEmpty() && securityContextWrapper.getAuthentication() == null)
            securityContextWrapper.setAuthentication(new JWTAuthToken(jwt));
        doFilter(request, response, filterChain);
    }

    private boolean checkBearer(@NonNull HttpServletRequest request) {
        String header = request.getHeader(HEADER_NAME);
        return !StringUtils.isEmpty(header) && header.startsWith(BEARER_PREFIX);
    }
}
