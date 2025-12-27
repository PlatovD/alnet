package io.github.platovd.alnet.handler.provider;

import io.github.platovd.alnet.exception.entity.authentication.InvalidAccessTokenException;
import io.github.platovd.alnet.exception.entity.authentication.InvalidRefreshTokenException;
import io.github.platovd.alnet.exception.entity.authentication.UnknownTokenTypeException;
import io.jsonwebtoken.JwtException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.stereotype.Component;

@Component
public class JwtExceptionHandlerProvider implements ExceptionHandlerProvider {
    @Override
    public ProblemDetail handle(Exception exception) {
        if (!supports(exception.getClass()))
            throw new IllegalArgumentException("Exception provider get unsupported class to handle");
        if (exception instanceof InvalidAccessTokenException e) {
            return invalidAccessTokenException(e);
        }
        if (exception instanceof InvalidRefreshTokenException e) {
            return invalidRefreshTokenException(e);
        }
        if (exception instanceof UnknownTokenTypeException e) {
            return unknownTokenTypeExceptionHandler(e);
        }
        throw new RuntimeException("Unexpected jwt exception. " + exception.getClass().getName());
    }

    @Override
    public boolean supports(Class<?> clazz) {
        return JwtException.class.isAssignableFrom(clazz);
    }

    protected ProblemDetail invalidAccessTokenException(InvalidAccessTokenException e) {
        return ProblemDetail.forStatusAndDetail(
                HttpStatus.UNAUTHORIZED,
                "Given access token is not valid"
        );
    }

    protected ProblemDetail invalidRefreshTokenException(InvalidRefreshTokenException e) {
        return ProblemDetail.forStatusAndDetail(
                HttpStatus.UNAUTHORIZED,
                "Given refresh token is not valid"
        );
    }

    protected ProblemDetail unknownTokenTypeExceptionHandler(UnknownTokenTypeException e) {
        return ProblemDetail.forStatusAndDetail(
                HttpStatus.UNAUTHORIZED,
                "Given token has unsupported field 'typ'"
        );
    }
}
