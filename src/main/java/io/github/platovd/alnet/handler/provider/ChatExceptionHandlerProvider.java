package io.github.platovd.alnet.handler.provider;

import io.github.platovd.alnet.exception.entity.chat.ChatException;
import io.github.platovd.alnet.exception.entity.chat.ChatNotFoundException;
import io.github.platovd.alnet.exception.entity.chat.ChatUpdateException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.stereotype.Component;

@Component
public class ChatExceptionHandlerProvider implements ExceptionHandlerProvider {
    @Override
    public ProblemDetail handle(Exception exception) {
        if (!supports(exception.getClass()))
            throw new IllegalArgumentException("Exception provider get unsupported class to handle");

        if (exception instanceof ChatNotFoundException e) {
            return chatNotFoundExceptionHandler(e);
        }

        if (exception instanceof ChatUpdateException e) {
            return chatUpdateExceptionHandler(e);
        }

        throw new RuntimeException("Unexpected chat exception. " + exception.getClass().getName());
    }

    @Override
    public boolean supports(Class<?> clazz) {
        return ChatException.class.isAssignableFrom(clazz);
    }

    protected ProblemDetail chatNotFoundExceptionHandler(ChatNotFoundException e) {
        return ProblemDetail.forStatusAndDetail(
                HttpStatus.NOT_FOUND,
                "Chat entity wasn't found"
        );
    }

    protected ProblemDetail chatUpdateExceptionHandler(ChatUpdateException e) {
        return ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST,
                "Chat update failed. Request may was wrong"
        );
    }
}
