package io.github.platovd.alnet.service;

import io.github.platovd.alnet.entity.Role;
import io.github.platovd.alnet.entity.User;
import io.github.platovd.alnet.testutil.FabricForTests;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Date;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class JWTServiceTest {
    private final String key = "2cb238a48eb4276cd65622ccc501d640bb01b5114a7452e4599394843a47fe77";
    private final Long accessTokenExpirationDuration = 2L;
    private final Long refreshTokenExpirationDuration = 2L;
    private final Long noExpiration = 1000L;
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
        jwtService.setKey(key);
        jwtService.setAccessTokenExpirationDuration(accessTokenExpirationDuration);
        jwtService.setRefreshTokenExpirationDuration(refreshTokenExpirationDuration);

        correctNotExpiredAccessJwt = Jwts.builder().header().add("typ", "JWT").and()
                .id(String.valueOf(FabricForTests.USER_ID)).subject(testUser.getUsername()).claims(Map.of("type", "access"))
                .issuedAt(new Date(System.currentTimeMillis())).expiration(new Date(System.currentTimeMillis() +
                        accessTokenExpirationDuration * noExpiration))
                .signWith(Keys.hmacShaKeyFor(Decoders.BASE64.decode(key))).compact();

        correctNotExpiredRefreshJwt = Jwts.builder().header().add("typ", "JWT").and()
                .id(String.valueOf(FabricForTests.USER_ID)).subject(testUser.getUsername()).claims(Map.of("type", "refresh"))
                .issuedAt(new Date(System.currentTimeMillis())).expiration(new Date(System.currentTimeMillis() +
                        refreshTokenExpirationDuration * noExpiration))
                .signWith(Keys.hmacShaKeyFor(Decoders.BASE64.decode(key))).compact();

        incorrectNotExpiredAccessJwt = Jwts.builder().header().add("typ", "JWT").and()
                .id(String.valueOf(FabricForTests.USER_ID)).subject(testUser.getUsername()).claims(Map.of("type", "access"))
                .issuedAt(new Date(System.currentTimeMillis())).expiration(new Date(System.currentTimeMillis() +
                        accessTokenExpirationDuration * noExpiration))
                .signWith(Keys.hmacShaKeyFor(Decoders.BASE64.decode(key + "error"))).compact();

        incorrectNotExpiredRefreshJwt = Jwts.builder().header().add("typ", "JWT").and()
                .id(String.valueOf(FabricForTests.USER_ID)).subject(testUser.getUsername()).claims(Map.of("type", "refresh"))
                .issuedAt(new Date(System.currentTimeMillis())).expiration(new Date(System.currentTimeMillis() +
                        refreshTokenExpirationDuration * noExpiration))
                .signWith(Keys.hmacShaKeyFor(Decoders.BASE64.decode(key + "error"))).compact();

        expiredAccessJwt = Jwts.builder().header().add("typ", "JWT").and()
                .id(String.valueOf(FabricForTests.USER_ID)).subject(testUser.getUsername()).claims(Map.of("type", "access"))
                .issuedAt(new Date(System.currentTimeMillis())).expiration(new Date(System.currentTimeMillis() +
                        accessTokenExpirationDuration * (-noExpiration)))
                .signWith(Keys.hmacShaKeyFor(Decoders.BASE64.decode(key))).compact();

        expiredRefreshJwt = Jwts.builder().header().add("typ", "JWT").and()
                .id(String.valueOf(FabricForTests.USER_ID)).subject(testUser.getUsername()).claims(Map.of("type", "refresh"))
                .issuedAt(new Date(System.currentTimeMillis())).expiration(new Date(System.currentTimeMillis() +
                        refreshTokenExpirationDuration * (-noExpiration)))
                .signWith(Keys.hmacShaKeyFor(Decoders.BASE64.decode(key))).compact();
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
