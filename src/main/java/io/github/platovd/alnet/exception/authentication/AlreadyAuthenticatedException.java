package io.github.platovd.alnet.exception.authentication;

import org.springframework.security.core.AuthenticationException;

public class AlreadyAuthenticatedException extends AuthenticationException {
    public AlreadyAuthenticatedException(String message) {
        super(message);
    }
}
