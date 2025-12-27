package io.github.platovd.alnet.handler;

import io.github.platovd.alnet.exception.BaseException;
import io.github.platovd.alnet.handler.manager.ExceptionHandlerManager;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@ControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {
    private final ExceptionHandlerManager exceptionHandlerManager;

    @ExceptionHandler({BaseException.class, JwtException.class})
    public ResponseEntity<ProblemDetail> handleException(Exception exception) {
        return ResponseEntity.of(exceptionHandlerManager.handle(exception)).build();
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ProblemDetail> handleRemainingException(Exception ex) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Happened unexpected and unhanded exception"
        );
        return ResponseEntity.status(500).body(pd);
    }
}
