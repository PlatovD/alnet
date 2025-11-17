package io.github.platovd.alnet.exception;

import org.springframework.security.core.AuthenticationException;

public class IllegalTokenClassException extends AuthenticationException {
    public IllegalTokenClassException(String msg, Throwable cause) {
        super(msg, cause);
    }

    public IllegalTokenClassException(String msg) {
        super(msg);
    }
}
