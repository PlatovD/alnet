package io.github.platovd.alnet.testutil;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jsonpatch.JsonPatch;
import io.github.platovd.alnet.authentication.token.JWTAuthToken;
import io.github.platovd.alnet.dto.authentication.request.RefreshRequest;
import io.github.platovd.alnet.dto.authentication.request.SignInRequest;
import io.github.platovd.alnet.dto.authentication.request.SignUpRequest;
import io.github.platovd.alnet.dto.chat.request.ChatCreationOrUpdateRequest;
import io.github.platovd.alnet.entity.Chat;
import io.github.platovd.alnet.entity.Membership;
import io.github.platovd.alnet.entity.Role;
import io.github.platovd.alnet.entity.User;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.core.userdetails.UserDetails;

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
    public static final Long CHAT_ID = 10L;
    public static final String CHAT_NAME = "Test Chat";
    public static final String UPDATED_CHAT_NAME = "Updated Chat Name";
    public static final String ACCESS_TOKEN_TYPE = "access";
    public static final String REFRESH_TOKEN_TYPE = "refresh";
    public static final String INVALID_TOKEN_TYPE = "invalid";
    public static final Long CHAT_ID_2 = 20L;
    public static final String CHAT_NAME_2 = "Another Chat";
    public static final String NEW_USERNAME = "NewUser";
    public static final String NEW_EMAIL = "new@gmail.com";
    public static final String NEW_PASSWORD = "newpassword123";


    public static User testUser() {
        return User.builder().userId(USER_ID).email(EMAIL).username(USERNAME).password(PASSWORD).role(
                List.of(Role.builder().name(ROLE).build())).build();
    }

    public static SignUpRequest signUpRequest() {
        return SignUpRequest.builder().username(USERNAME).email(EMAIL).password(PASSWORD).build();
    }

    public static SignInRequest signInRequest() {
        return SignInRequest.builder().username(USERNAME).password(PASSWORD).build();
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

    public static Chat testChat() {
        return Chat.builder()
                .chatId(CHAT_ID)
                .chatName(CHAT_NAME)
                .build();
    }

    public static ChatCreationOrUpdateRequest chatUpdateRequest() {
        return ChatCreationOrUpdateRequest.builder()
                .chatId(CHAT_ID)
                .name(UPDATED_CHAT_NAME)
                .build();
    }

    public static ChatCreationOrUpdateRequest chatUpdateRequestWithDifferentId() {
        return ChatCreationOrUpdateRequest.builder()
                .chatId(CHAT_ID + 1)
                .name(UPDATED_CHAT_NAME)
                .build();
    }

    public static String accessTokenWithCustomExpiration(Long expirationMillis) {
        return Jwts.builder().header().add("typ", "JWT").and()
                .id(String.valueOf(USER_ID)).subject(USERNAME).claims(Map.of("type", "access"))
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + expirationMillis))
                .signWith(Keys.hmacShaKeyFor(Decoders.BASE64.decode(key))).compact();
    }

    public static String refreshTokenWithCustomExpiration(Long expirationMillis) {
        return Jwts.builder().header().add("typ", "JWT").and()
                .id(String.valueOf(USER_ID)).subject(USERNAME).claims(Map.of("type", "refresh"))
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + expirationMillis))
                .signWith(Keys.hmacShaKeyFor(Decoders.BASE64.decode(key))).compact();
    }

    public static String tokenWithCustomType(String type) {
        return Jwts.builder().header().add("typ", "JWT").and()
                .id(String.valueOf(USER_ID)).subject(USERNAME).claims(Map.of("type", type))
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + 1000000L))
                .signWith(Keys.hmacShaKeyFor(Decoders.BASE64.decode(key))).compact();
    }

    public static Chat anotherTestChat() {
        return Chat.builder()
                .chatId(CHAT_ID_2)
                .chatName(CHAT_NAME_2)
                .build();
    }

    public static User anotherTestUser() {
        return User.builder()
                .userId(USER_ID + 1)
                .email("another@gmail.com")
                .username("AnotherUser")
                .password(PASSWORD)
                .role(List.of(Role.builder().name(ROLE).build()))
                .build();
    }

    public static Membership testMembership() {
        return Membership.builder()
                .user(testUser())
                .chat(testChat())
                .build();
    }

    public static Membership anotherTestMembership() {
        return Membership.builder()
                .user(anotherTestUser())
                .chat(anotherTestChat())
                .build();
    }

    public static List<User> testUserList() {
        return List.of(testUser(), anotherTestUser());
    }

    public static JWTAuthToken testJWTAuthToken() {
        // Создаем реальный JWTAuthToken с JWT строкой
        JWTAuthToken token = new JWTAuthToken(FabricForTests.correctNoExpiredAccessJwt());
        token.setAuthenticated(true);
        // ID будет извлечен из токена при вызове getId()
        return token;
    }

    public static JWTAuthToken testJWTAuthTokenWithSpecificId(Long userId) {
        // Создаем кастомный JWT с нужным ID
        String customJwt = Jwts.builder()
                .header().add("typ", "JWT").and()
                .id(String.valueOf(userId))
                .subject(FabricForTests.USERNAME)
                .claims(Map.of("type", "access"))
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + 1000000L))
                .signWith(Keys.hmacShaKeyFor(Decoders.BASE64.decode(FabricForTests.key)))
                .compact();

        JWTAuthToken token = new JWTAuthToken(customJwt);
        token.setAuthenticated(true);
        return token;
    }

    // Добавляем для тестов с JsonPatch
    public static JsonPatch createEmailUpdatePatch(String newEmail) {
        try {
            String patchStr = String.format(
                    "[{\"op\":\"replace\",\"path\":\"/email\",\"value\":\"%s\"}]",
                    newEmail
            );
            return JsonPatch.fromJson(new ObjectMapper().readTree(patchStr));
        } catch (Exception e) {
            throw new RuntimeException("Failed to create email update patch", e);
        }
    }

    public static JsonPatch createUsernameUpdatePatch(String newUsername) {
        try {
            String patchStr = String.format(
                    "[{\"op\":\"replace\",\"path\":\"/username\",\"value\":\"%s\"}]",
                    newUsername
            );
            return JsonPatch.fromJson(new ObjectMapper().readTree(patchStr));
        } catch (Exception e) {
            throw new RuntimeException("Failed to create username update patch", e);
        }
    }

    public static JsonPatch createInvalidPatch() {
        try {
            String patchStr = "[{\"op\":\"remove\",\"path\":\"/userId\"}]";
            return JsonPatch.fromJson(new ObjectMapper().readTree(patchStr));
        } catch (Exception e) {
            throw new RuntimeException("Failed to create invalid patch", e);
        }
    }

    // Для UserService
    public static User testUserWithDifferentId(Long userId) {
        return User.builder()
                .userId(userId)
                .email(FabricForTests.EMAIL)
                .username(FabricForTests.USERNAME)
                .password(FabricForTests.PASSWORD)
                .role(List.of(Role.builder().name(FabricForTests.ROLE).build()))
                .build();
    }

    public static User testUserWithDifferentUsername(String username) {
        return User.builder()
                .userId(FabricForTests.USER_ID)
                .email(FabricForTests.EMAIL)
                .username(username)
                .password(FabricForTests.PASSWORD)
                .role(List.of(Role.builder().name(FabricForTests.ROLE).build()))
                .build();
    }
}
