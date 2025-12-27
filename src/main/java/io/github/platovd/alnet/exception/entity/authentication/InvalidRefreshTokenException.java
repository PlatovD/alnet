package io.github.platovd.alnet.exception.entity.authentication;

import io.jsonwebtoken.JwtException;

public class InvalidRefreshTokenException extends JwtException {
    public InvalidRefreshTokenException(String message) {
        super(message);
    }
}
