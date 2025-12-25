package io.github.platovd.alnet.exception.chat;

public abstract class ChatServiceException extends RuntimeException {
    public ChatServiceException() {
    }

    public ChatServiceException(String message) {
        super(message);
    }
}
