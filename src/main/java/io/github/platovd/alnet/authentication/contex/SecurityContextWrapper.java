package io.github.platovd.alnet.authentication.contex;

import io.github.platovd.alnet.exception.NoAuthenticationCredentialsException;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.function.Function;

/**
 * Класс, реализующий обертку над SecurityContextHolder. Необходим лишь для того, чтобы взаимодействовать с
 * SecurityContextHolder через удобный интерфейс и выполнять сразу несколько операций над контекстом с помощью
 * отдельных функций.
 */
@Component
@RequiredArgsConstructor
@Setter
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

    public <T> T getAuthenticatedUserInfo(Function<UserDetails, T> resolver) {
        if (!isAuthenticated())
            throw new NoAuthenticationCredentialsException("User authentication is not strong or not exists");

        Object principal = getAuthentication().getPrincipal();
        if (!(principal instanceof UserDetails details))
            throw new NoAuthenticationCredentialsException("Principal is unsupported type " + principal.getClass().getName());

        return resolver.apply(details);
    }

    public void unAuthenticate() {
        SecurityContext context = SecurityContextHolder.getContext();
        try {
            context.setAuthentication(
                    new AnonymousAuthenticationToken(
                            anonymousAuthKey,
                            "anonymousUser",
                            List.of(new SimpleGrantedAuthority("ROLE_ANONYMOUS")))
            );
        } catch (IllegalArgumentException e) {
            anonymousAuthKey = "Error key";
        }

    }

    public Authentication getAuthentication() {
        return getContext().getAuthentication();
    }

    public void setAuthentication(Authentication authentication) {
        unAuthenticate();
        getContext().setAuthentication(authentication);
    }
}
