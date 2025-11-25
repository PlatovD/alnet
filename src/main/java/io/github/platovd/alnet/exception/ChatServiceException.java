package io.github.platovd.alnet.exception;

public abstract class ChatServiceException extends RuntimeException {
    public ChatServiceException() {
    }

    public ChatServiceException(String message) {
        super(message);
    }
}
