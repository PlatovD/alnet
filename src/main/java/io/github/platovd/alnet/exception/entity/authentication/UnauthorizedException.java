package io.github.platovd.alnet.exception.entity.authentication;

public class UnauthorizedException extends AuthenticationException {
    public UnauthorizedException(String message) {
        super(message);
    }
}
