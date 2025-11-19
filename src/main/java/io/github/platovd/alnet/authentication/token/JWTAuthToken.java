package io.github.platovd.alnet.authentication.token;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;

public class JWTAuthToken implements Authentication {
    private final String jwtToken;
    private UserDetails principal;
    private Collection<? extends GrantedAuthority> authorities;
    private boolean isAuthenticated = false;

    public JWTAuthToken(String jwtToken) {
        this.jwtToken = jwtToken;
    }

    public JWTAuthToken(String jwtToken, UserDetails principal, Collection<? extends GrantedAuthority> authorities, boolean isAuthenticated) {
        this.jwtToken = jwtToken;
        this.principal = principal;
        this.authorities = authorities;
        this.isAuthenticated = isAuthenticated;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public Object getCredentials() {
        return jwtToken;
    }

    @Override
    public Object getDetails() {
        return null;
    }

    @Override
    public Object getPrincipal() {
        return principal;
    }

    @Override
    public boolean isAuthenticated() {
        return isAuthenticated;
    }

    @Override
    public void setAuthenticated(boolean isAuthenticated) throws IllegalArgumentException {
        this.isAuthenticated = isAuthenticated;
    }

    @Override
    public String getName() {
        if (principal == null) return "";
        return principal.getUsername();
    }

    public String getToken() {
        return jwtToken;
    }
}
