package io.github.platovd.alnet.exception.entity.authentication;

public class NoAuthenticationCredentialsException extends AuthenticationException {
    public NoAuthenticationCredentialsException(String message) {
        super(message);
    }
}
