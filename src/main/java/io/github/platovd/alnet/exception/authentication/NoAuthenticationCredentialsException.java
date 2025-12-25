package io.github.platovd.alnet.exception.authentication;

public class NoAuthenticationCredentialsException extends RuntimeException {
    public NoAuthenticationCredentialsException(String message) {
        super(message);
    }
}
