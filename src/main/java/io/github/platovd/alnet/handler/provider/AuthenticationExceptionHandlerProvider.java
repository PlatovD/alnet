package io.github.platovd.alnet.handler.provider;

import io.github.platovd.alnet.exception.entity.authentication.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.stereotype.Component;

@Component
public class AuthenticationExceptionHandlerProvider implements ExceptionHandlerProvider {
    @Override
    public ProblemDetail handle(Exception exception) {
        if (!supports(exception.getClass()))
            throw new IllegalArgumentException("Exception provider get unsupported class to handle");

        if (exception instanceof AlreadyAuthenticatedException e) {
            return alreadyAuthenticatedHandler(e);
        }

        if (exception instanceof IllegalTokenClassException e) {
            return unsupportedTokenTypeExceptionHandler(e);
        }

        if (exception instanceof NoAuthenticationCredentialsException e) {
            return noAuthenticationCredentialsExceptionHandler(e);
        }

        if (exception instanceof UnauthorizedException e) {
            return unauthorizedExceptionHandler(e);
        }

        if (exception instanceof UnknownAuthenticationException e) {
            return unknownAuthenticationExceptionHandler(e);
        }

        if (exception instanceof WrongPasswordException e) {
            return wrongPasswordExceptionHandler(e);
        }

        throw new RuntimeException("Unexpected authentication exception. " + exception.getClass().getName());
    }

    @Override
    public boolean supports(Class<?> clazz) {
        return AuthenticationException.class.isAssignableFrom(clazz);
    }

    protected ProblemDetail alreadyAuthenticatedHandler(AlreadyAuthenticatedException e) {
        return ProblemDetail.forStatusAndDetail(
                HttpStatus.CONFLICT,
                "User already authenticated"
        );
    }

    protected ProblemDetail unsupportedTokenTypeExceptionHandler(IllegalTokenClassException e) {
        return ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST,
                "Wrong token type or class"
        );
    }

    protected ProblemDetail noAuthenticationCredentialsExceptionHandler(NoAuthenticationCredentialsException e) {
        return ProblemDetail.forStatusAndDetail(
                HttpStatus.UNAUTHORIZED,
                "No credentials found"
        );
    }

    protected ProblemDetail unauthorizedExceptionHandler(UnauthorizedException e) {
        return ProblemDetail.forStatusAndDetail(
                HttpStatus.FORBIDDEN,
                "No enough rights"
        );
    }

    protected ProblemDetail unknownAuthenticationExceptionHandler(UnknownAuthenticationException e) {
        return ProblemDetail.forStatusAndDetail(
                HttpStatus.UNAUTHORIZED,
                "Unknown authentication type"
        );
    }

    protected ProblemDetail wrongPasswordExceptionHandler(WrongPasswordException e) {
        return ProblemDetail.forStatusAndDetail(
                HttpStatus.UNAUTHORIZED,
                "Wrong password"
        );
    }
}
