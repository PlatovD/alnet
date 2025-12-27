package io.github.platovd.alnet.handler.manager;

import io.github.platovd.alnet.exception.BaseException;
import io.github.platovd.alnet.handler.provider.ExceptionHandlerProvider;
import org.springframework.http.ProblemDetail;

import java.util.ArrayList;
import java.util.List;

public class BasicExceptionHandlerManager implements ExceptionHandlerManager {
    private final List<ExceptionHandlerProvider> providers = new ArrayList<>();

    @Override
    public ProblemDetail handle(Exception baseException) {
        ProblemDetail detail = null;
        for (ExceptionHandlerProvider provider : providers) {
            if (provider.supports(baseException.getClass())) {
                detail = provider.handle(baseException);
            }
            if (detail != null) return detail;
        }
        // не нашли обработчика => делаем unexpected
        throw new RuntimeException("Unexpected");
    }

    @Override
    public ExceptionHandlerManager addProvider(ExceptionHandlerProvider provider) {
        providers.add(provider);
        return this;
    }
}
