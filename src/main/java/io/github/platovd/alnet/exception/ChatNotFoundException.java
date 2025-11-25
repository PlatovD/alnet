package io.github.platovd.alnet.exception;

public class ChatNotFoundException extends ChatServiceException {
    public ChatNotFoundException() {
    }

    public ChatNotFoundException(String message) {
        super(message);
    }
}
