package io.github.platovd.alnet.handler.provider;

import io.github.platovd.alnet.exception.entity.message.MessageContentException;
import io.github.platovd.alnet.exception.entity.message.MessageException;
import io.github.platovd.alnet.exception.entity.message.MessageNotFoundException;
import io.github.platovd.alnet.exception.entity.message.MessageUpdateException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.stereotype.Component;

@Component
public class MessageExceptionHandlerProvider implements ExceptionHandlerProvider {
    @Override
    public ProblemDetail handle(Exception exception) {
        if (!supports(exception.getClass()))
            throw new IllegalArgumentException("Exception provider get unsupported class to handle");

        if (exception instanceof MessageContentException e) {
            return messageContentExceptionHandler(e);
        }

        if (exception instanceof MessageNotFoundException e) {
            return messageNotFoundExceptionHandler(e);
        }

        if (exception instanceof MessageUpdateException e) {
            return messageUpdateExceptionHandler(e);
        }

        throw new RuntimeException("Unexpected message exception. " + exception.getClass().getName());
    }

    @Override
    public boolean supports(Class<?> clazz) {
        return MessageException.class.isAssignableFrom(clazz);
    }

    protected ProblemDetail messageContentExceptionHandler(MessageContentException e) {
        return ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST,
                "Message content isn't valid. Check it for not being blank"
        );
    }

    protected ProblemDetail messageNotFoundExceptionHandler(MessageNotFoundException e) {
        return ProblemDetail.forStatusAndDetail(
                HttpStatus.NOT_FOUND,
                "Message wasn't found"
        );
    }

    protected ProblemDetail messageUpdateExceptionHandler(MessageUpdateException e) {
        return ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST,
                "Message update request isn't valid"
        );
    }
}
