package io.github.platovd.alnet.exception.entity.authentication;

public class WrongPasswordException extends AuthenticationException {
    public WrongPasswordException(String message) {
        super(message);
    }
}
