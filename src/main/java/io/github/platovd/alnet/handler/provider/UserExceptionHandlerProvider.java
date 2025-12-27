package io.github.platovd.alnet.handler.provider;

import io.github.platovd.alnet.exception.entity.user.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.stereotype.Component;

@Component
public class UserExceptionHandlerProvider implements ExceptionHandlerProvider {

    @Override
    public ProblemDetail handle(Exception exception) {
        if (!supports(exception.getClass()))
            throw new IllegalArgumentException("Exception provider get unsupported class to handle");

        if (exception instanceof EmailUsedException e) {
            return emailUsedExceptionHandler(e);
        }
        if (exception instanceof IdNotFoundException e) {
            return idNotFoundExceptionHandler(e);
        }
        if (exception instanceof UsernameNotFoundException e) {
            return usernameNotFoundExceptionHandler(e);
        }
        if (exception instanceof UsernameUsedException e) {
            return usernameUsedExceptionHandler(e);
        }

        throw new RuntimeException("Unexpected user exception. " + exception.getClass().getName());
    }

    @Override
    public boolean supports(Class<?> clazz) {
        return UserException.class.isAssignableFrom(clazz);
    }

    protected ProblemDetail emailUsedExceptionHandler(EmailUsedException e) {
        return ProblemDetail.forStatusAndDetail(
                HttpStatus.CONFLICT,
                "This email address is already in use"
        );
    }

    protected ProblemDetail idNotFoundExceptionHandler(IdNotFoundException e) {
        return ProblemDetail.forStatusAndDetail(
                HttpStatus.NOT_FOUND,
                "User with given ID wasn't found"
        );
    }

    protected ProblemDetail usernameNotFoundExceptionHandler(UsernameNotFoundException e) {
        return ProblemDetail.forStatusAndDetail(
                HttpStatus.NOT_FOUND,
                "User with given username wasn't found"
        );
    }

    protected ProblemDetail usernameUsedExceptionHandler(UsernameUsedException e) {
        return ProblemDetail.forStatusAndDetail(
                HttpStatus.CONFLICT,
                "This username is already taken"
        );
    }
}

