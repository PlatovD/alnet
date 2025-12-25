package io.github.platovd.alnet.exception.authentication;

import org.springframework.security.core.AuthenticationException;

public class UnknownAuthenticationException extends AuthenticationException {
    public UnknownAuthenticationException(String msg, Throwable cause) {
        super(msg, cause);
    }

    public UnknownAuthenticationException(String msg) {
        super(msg);
    }
}
