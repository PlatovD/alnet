package io.github.platovd.alnet.exception.user;

public class UsernameNotFoundException extends UserServiceException {
    public UsernameNotFoundException(String message) {
        super(message);
    }
}
