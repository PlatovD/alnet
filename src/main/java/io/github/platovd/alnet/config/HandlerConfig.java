package io.github.platovd.alnet.config;

import io.github.platovd.alnet.handler.manager.BasicExceptionHandlerManager;
import io.github.platovd.alnet.handler.manager.ExceptionHandlerManager;
import io.github.platovd.alnet.handler.provider.ExceptionHandlerProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class HandlerConfig {
    @Bean
    public ExceptionHandlerManager exceptionHandlerManager(List<ExceptionHandlerProvider> providers) {
        ExceptionHandlerManager exceptionManager = new BasicExceptionHandlerManager();
        providers.forEach(exceptionManager::addProvider);
        return exceptionManager;
    }
}
