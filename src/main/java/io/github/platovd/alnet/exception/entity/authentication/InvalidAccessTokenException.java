package io.github.platovd.alnet.exception.entity.authentication;

import io.jsonwebtoken.JwtException;

public class InvalidAccessTokenException extends JwtException {
    public InvalidAccessTokenException(String message) {
        super(message);
    }
}
