package io.github.platovd.alnet.authentication.contex;

import io.github.platovd.alnet.exception.NoAuthenticationCredentialsException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class SecurityContextWrapper {
    @Value("${auth.anonymous.key}")
    private String anonymousAuthKey;


    public SecurityContext getContext() {
        return SecurityContextHolder.getContext();
    }

    public boolean isAuthenticated() {
        SecurityContext context = SecurityContextHolder.getContext();
        Authentication authentication = context.getAuthentication();

        return authentication != null
                && !(authentication instanceof AnonymousAuthenticationToken)
                && authentication.isAuthenticated();
    }

    public Object getAuthenticationCredentials() {
        SecurityContext context = SecurityContextHolder.getContext();
        Authentication authentication = context.getAuthentication();
        if (!isAuthenticated())
            throw new NoAuthenticationCredentialsException("User authentication is not strong or not exists");
        return authentication.getCredentials();
    }

    public void unAuthenticate() {
        SecurityContext context = SecurityContextHolder.getContext();
        context.setAuthentication(
                new AnonymousAuthenticationToken(
                        anonymousAuthKey,
                        "anonymousUser",
                        List.of(new SimpleGrantedAuthority("ROLE_ANONYMOUS")))
        );
    }

    public Authentication getAuthentication() {
        return getContext().getAuthentication();
    }

    public void setAuthentication(Authentication authentication) {
        unAuthenticate();
        getContext().setAuthentication(authentication);
    }
}
