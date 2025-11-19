package io.github.platovd.alnet.exception;

import org.springframework.security.core.AuthenticationException;

public class InvalidAccessTokenException extends AuthenticationException {
    public InvalidAccessTokenException(String message) {
        super(message);
    }
}
