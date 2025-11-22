package io.github.platovd.alnet.testutil;

import io.github.platovd.alnet.dto.request.RefreshRequest;
import io.github.platovd.alnet.dto.request.SignInRequest;
import io.github.platovd.alnet.dto.request.SignUpRequest;
import io.github.platovd.alnet.entity.Role;
import io.github.platovd.alnet.entity.User;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

import java.util.Date;
import java.util.List;
import java.util.Map;

public class FabricForTests {
    public static final String JWT = "jwt";
    public static final Long USER_ID = 1L;
    public static final String USERNAME = "Test";
    public static final String PASSWORD = "qwerty";
    public static final String EMAIL = "test@gmail.com";
    public static final String ROLE = "USER";
    public static final String ANONYMOUS_KEY = "KEY";
    public static final String key = "2cb238a48eb4276cd65622ccc501d640bb01b5114a7452e4599394843a47fe77";
    public static final Long accessTokenExpirationDuration = 2L;
    public static final Long refreshTokenExpirationDuration = 2L;
    public static final Long noExpiration = 1000L;

    public static User testUser() {
        return User.builder().id(USER_ID).email(EMAIL).username(USERNAME).password(PASSWORD).role(
                List.of(Role.builder().name(ROLE).build())).build();
    }

    public static SignUpRequest signUpRequest() {
        return SignUpRequest.builder().name(USERNAME).email(EMAIL).password(PASSWORD).build();
    }

    public static SignInRequest signInRequest() {
        return SignInRequest.builder().name(USERNAME).password(PASSWORD).build();
    }

    public static RefreshRequest refreshRequest() {
        return RefreshRequest.builder().refresh(key).build();
    }

    public static String correctNoExpiredAccessJwt() {
        return Jwts.builder().header().add("typ", "JWT").and()
                .id(String.valueOf(FabricForTests.USER_ID)).subject(testUser().getUsername()).claims(Map.of("type", "access"))
                .issuedAt(new Date(System.currentTimeMillis())).expiration(new Date(System.currentTimeMillis() +
                        FabricForTests.accessTokenExpirationDuration * noExpiration))
                .signWith(Keys.hmacShaKeyFor(Decoders.BASE64.decode(FabricForTests.key))).compact();
    }

    public static String correctNoExpiredRefreshJwt() {
        return Jwts.builder().header().add("typ", "JWT").and()
                .id(String.valueOf(FabricForTests.USER_ID)).subject(testUser().getUsername()).claims(Map.of("type", "refresh"))
                .issuedAt(new Date(System.currentTimeMillis())).expiration(new Date(System.currentTimeMillis() +
                        FabricForTests.refreshTokenExpirationDuration * noExpiration))
                .signWith(Keys.hmacShaKeyFor(Decoders.BASE64.decode(FabricForTests.key))).compact();
    }

    public static String incorrectNotExpiredAccessJwt() {
        return Jwts.builder().header().add("typ", "JWT").and()
                .id(String.valueOf(FabricForTests.USER_ID)).subject(testUser().getUsername()).claims(Map.of("type", "access"))
                .issuedAt(new Date(System.currentTimeMillis())).expiration(new Date(System.currentTimeMillis() +
                        FabricForTests.accessTokenExpirationDuration * noExpiration))
                .signWith(Keys.hmacShaKeyFor(Decoders.BASE64.decode(FabricForTests.key + "error"))).compact();
    }

    public static String incorrectNotExpiredRefreshJwt() {
        return Jwts.builder().header().add("typ", "JWT").and()
                .id(String.valueOf(FabricForTests.USER_ID)).subject(testUser().getUsername()).claims(Map.of("type", "refresh"))
                .issuedAt(new Date(System.currentTimeMillis())).expiration(new Date(System.currentTimeMillis() +
                        FabricForTests.refreshTokenExpirationDuration * noExpiration))
                .signWith(Keys.hmacShaKeyFor(Decoders.BASE64.decode(FabricForTests.key + "error"))).compact();
    }

    public static String expiredAccessJwt() {
        return Jwts.builder().header().add("typ", "JWT").and()
                .id(String.valueOf(FabricForTests.USER_ID)).subject(testUser().getUsername()).claims(Map.of("type", "access"))
                .issuedAt(new Date(System.currentTimeMillis())).expiration(new Date(System.currentTimeMillis() +
                        FabricForTests.accessTokenExpirationDuration * (-noExpiration)))
                .signWith(Keys.hmacShaKeyFor(Decoders.BASE64.decode(FabricForTests.key))).compact();
    }

    public static String expiredRefreshJwt() {
        return Jwts.builder().header().add("typ", "JWT").and()
                .id(String.valueOf(FabricForTests.USER_ID)).subject(testUser().getUsername()).claims(Map.of("type", "refresh"))
                .issuedAt(new Date(System.currentTimeMillis())).expiration(new Date(System.currentTimeMillis() +
                        FabricForTests.refreshTokenExpirationDuration * (-noExpiration)))
                .signWith(Keys.hmacShaKeyFor(Decoders.BASE64.decode(FabricForTests.key))).compact();
    }
}
