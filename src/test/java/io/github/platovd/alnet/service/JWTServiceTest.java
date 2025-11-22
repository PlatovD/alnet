package io.github.platovd.alnet.service;

import io.github.platovd.alnet.entity.Role;
import io.github.platovd.alnet.entity.User;
import io.github.platovd.alnet.testutil.FabricForTests;
import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class JWTServiceTest {

    private JWTService jwtService;
    private User testUser;

    private String correctNotExpiredAccessJwt;
    private String correctNotExpiredRefreshJwt;

    private String incorrectNotExpiredAccessJwt;
    private String incorrectNotExpiredRefreshJwt;

    private String expiredAccessJwt;
    private String expiredRefreshJwt;

    @BeforeEach
    public void setUp() {
        testUser = new User(
                FabricForTests.USER_ID, FabricForTests.USERNAME, FabricForTests.PASSWORD, "test@gmail.com", List.of(new Role(FabricForTests.ROLE, List.of())),
                null);
        jwtService = new JWTService();
        jwtService.setKey(FabricForTests.key);
        jwtService.setAccessTokenExpirationDuration(FabricForTests.accessTokenExpirationDuration);
        jwtService.setRefreshTokenExpirationDuration(FabricForTests.refreshTokenExpirationDuration);

        correctNotExpiredAccessJwt = FabricForTests.correctNoExpiredAccessJwt();

        correctNotExpiredRefreshJwt = FabricForTests.correctNoExpiredRefreshJwt();

        incorrectNotExpiredAccessJwt = FabricForTests.incorrectNotExpiredAccessJwt();

        incorrectNotExpiredRefreshJwt = FabricForTests.incorrectNotExpiredRefreshJwt();

        expiredAccessJwt = FabricForTests.expiredAccessJwt();

        expiredRefreshJwt = FabricForTests.expiredRefreshJwt();
    }

    @Test
    public void extractCorrectTokenTest() {
        assertThat(jwtService.extractUserName(correctNotExpiredAccessJwt)).isEqualTo(FabricForTests.USERNAME);
        assertThat(jwtService.extractId(correctNotExpiredAccessJwt)).isEqualTo(FabricForTests.USER_ID);
        assertThat(jwtService.extractTokenType(correctNotExpiredAccessJwt)).isEqualTo("access");
        assertThat(jwtService.extractUserName(correctNotExpiredRefreshJwt)).isEqualTo(FabricForTests.USERNAME);
        assertThat(jwtService.extractId(correctNotExpiredRefreshJwt)).isEqualTo(FabricForTests.USER_ID);
        assertThat(jwtService.extractTokenType(correctNotExpiredRefreshJwt)).isEqualTo("refresh");
    }

    @Test
    public void extractIncorrectTokenTest() {
        assertThatThrownBy(() -> jwtService.extractUserName(incorrectNotExpiredAccessJwt)).isInstanceOf(JwtException.class);
        assertThatThrownBy(() -> jwtService.extractId(incorrectNotExpiredAccessJwt)).isInstanceOf(JwtException.class);
        assertThatThrownBy(() -> jwtService.extractTokenType(incorrectNotExpiredAccessJwt)).isInstanceOf(JwtException.class);
        assertThatThrownBy(() -> jwtService.extractUserName(expiredAccessJwt)).isInstanceOf(JwtException.class);
        assertThatThrownBy(() -> jwtService.extractId(expiredAccessJwt)).isInstanceOf(JwtException.class);
        assertThatThrownBy(() -> jwtService.extractTokenType(expiredAccessJwt)).isInstanceOf(JwtException.class);

        assertThatThrownBy(() -> jwtService.extractUserName(incorrectNotExpiredRefreshJwt)).isInstanceOf(JwtException.class);
        assertThatThrownBy(() -> jwtService.extractId(incorrectNotExpiredRefreshJwt)).isInstanceOf(JwtException.class);
        assertThatThrownBy(() -> jwtService.extractTokenType(incorrectNotExpiredRefreshJwt)).isInstanceOf(JwtException.class);
        assertThatThrownBy(() -> jwtService.extractUserName(expiredRefreshJwt)).isInstanceOf(JwtException.class);
        assertThatThrownBy(() -> jwtService.extractId(expiredRefreshJwt)).isInstanceOf(JwtException.class);
        assertThatThrownBy(() -> jwtService.extractTokenType(expiredRefreshJwt)).isInstanceOf(JwtException.class);
    }

    @Test
    public void isTokenValidCorrectTest() {
        assertThat(jwtService.isTokenValid(correctNotExpiredAccessJwt, testUser)).isTrue();
    }

    @Test
    public void isTokenValidIncorrectTest() {
        assertThat(jwtService.isTokenValid(incorrectNotExpiredAccessJwt, testUser)).isFalse();
        assertThat(jwtService.isTokenValid(expiredAccessJwt, testUser)).isFalse();
    }
}
