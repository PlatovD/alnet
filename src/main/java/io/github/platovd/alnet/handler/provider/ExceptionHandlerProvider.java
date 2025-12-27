package io.github.platovd.alnet.handler.provider;

import io.github.platovd.alnet.exception.BaseException;
import org.springframework.http.ProblemDetail;

public interface ExceptionHandlerProvider {
    ProblemDetail handle(Exception exception);

    boolean supports(Class<?> clazz);
}
