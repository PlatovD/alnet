package io.github.platovd.alnet.exception.entity.authentication;

public class AlreadyAuthenticatedException extends AuthenticationException {
    public AlreadyAuthenticatedException(String message) {
        super(message);
    }
}
