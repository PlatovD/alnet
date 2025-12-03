package io.github.platovd.alnet.exception.authentication;

import io.jsonwebtoken.JwtException;

public class UnknownTokenTypeException extends JwtException {
    public UnknownTokenTypeException(String message) {
        super(message);
    }
}
