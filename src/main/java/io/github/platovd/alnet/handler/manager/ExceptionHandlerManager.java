package io.github.platovd.alnet.handler.manager;

import io.github.platovd.alnet.handler.provider.ExceptionHandlerProvider;
import org.springframework.http.ProblemDetail;

public interface ExceptionHandlerManager {
    ProblemDetail handle(Exception exception);

    ExceptionHandlerManager addProvider(ExceptionHandlerProvider provider);
}
