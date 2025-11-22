package io.github.platovd.alnet.testutil;

import io.github.platovd.alnet.entity.Role;
import io.github.platovd.alnet.entity.User;

import java.util.List;

public class FabricForTests {
    public static final String JWT = "jwt";
    public static final Long USER_ID = 1L;
    public static final String USERNAME = "Test";
    public static final String PASSWORD = "qwerty";
    public static final String ROLE = "USER";
    public static final String ANONYMOUS_KEY = "KEY";

    public static User testUser() {
        return User.builder().id(USER_ID).email("test@gmail.com").username(USERNAME).password("qwerty").role(
                List.of(Role.builder().name(ROLE).build())).build();
    }
}
