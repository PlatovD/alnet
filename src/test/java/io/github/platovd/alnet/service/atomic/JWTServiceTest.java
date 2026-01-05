package io.github.platovd.alnet.service.atomic;

import io.github.platovd.alnet.entity.User;
import io.github.platovd.alnet.exception.entity.authentication.UnknownTokenTypeException;
import io.github.platovd.alnet.testutil.FabricForTests;
import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(MockitoExtension.class)
class JWTServiceTest {

    @InjectMocks
    private JWTService jwtService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = FabricForTests.testUser();

        ReflectionTestUtils.setField(jwtService, "key", FabricForTests.key);
        ReflectionTestUtils.setField(jwtService, "accessTokenExpirationDuration", 3600L); // 1 час
        ReflectionTestUtils.setField(jwtService, "refreshTokenExpirationDuration", 86400L); // 24 часа
    }

    @Test
    void extractUserNameCorrectTest() {
        // given
        String token = FabricForTests.correctNoExpiredAccessJwt();

        // when
        String username = jwtService.extractUserName(token);

        // then
        assertThat(username).isEqualTo(FabricForTests.USERNAME);
    }

    @Test
    void extractUserNameShouldThrowJwtExceptionTest() {
        // given
        String invalidToken = "invalid.token.here";

        // when & then
        assertThatThrownBy(() -> jwtService.extractUserName(invalidToken))
                .isInstanceOf(JwtException.class);
    }

    @Test
    void extractIdCorrectTest() {
        // given
        String token = FabricForTests.correctNoExpiredAccessJwt();

        // when
        Long id = jwtService.extractId(token);

        // then
        assertThat(id).isEqualTo(FabricForTests.USER_ID);
    }

    @Test
    void extractIdShouldThrowJwtExceptionTest() {
        // given
        String invalidToken = FabricForTests.incorrectNotExpiredAccessJwt();

        // when & then
        assertThatThrownBy(() -> jwtService.extractId(invalidToken))
                .isInstanceOf(JwtException.class);
    }

    @Test
    void extractTokenTypeAccessCorrectTest() {
        // given
        String token = FabricForTests.correctNoExpiredAccessJwt();

        // when
        String tokenType = jwtService.extractTokenType(token);

        // then
        assertThat(tokenType).isEqualTo("access");
    }

    @Test
    void extractTokenTypeRefreshCorrectTest() {
        // given
        String token = FabricForTests.correctNoExpiredRefreshJwt();

        // when
        String tokenType = jwtService.extractTokenType(token);

        // then
        assertThat(tokenType).isEqualTo("refresh");
    }

    @Test
    void extractTokenTypeShouldThrowUnknownTokenTypeExceptionTest() {
        // given
        String malformedToken = "header.payload.signature";

        // when & then
        assertThatThrownBy(() -> jwtService.extractTokenType(malformedToken))
                .isInstanceOf(UnknownTokenTypeException.class);
    }

    @Test
    void isTokenValidCorrectTest() {
        // given
        String token = FabricForTests.correctNoExpiredAccessJwt();

        // when
        boolean isValid = jwtService.isTokenValid(token, testUser);

        // then
        assertThat(isValid).isTrue();
    }

    @Test
    void isTokenValidWithWrongUsernameTest() {
        // given
        String token = FabricForTests.correctNoExpiredAccessJwt();
        User wrongUser = User.builder()
                .userId(FabricForTests.USER_ID + 1)
                .username("WrongUser")
                .password(FabricForTests.PASSWORD)
                .build();

        // when
        boolean isValid = jwtService.isTokenValid(token, wrongUser);

        // then
        assertThat(isValid).isFalse();
    }

    @Test
    void isTokenValidWithExpiredTokenTest() {
        // given
        String token = FabricForTests.expiredAccessJwt();

        // when
        boolean isValid = jwtService.isTokenValid(token, testUser);

        // then
        assertThat(isValid).isFalse();
    }

    @Test
    void isTokenValidWithInvalidTokenTest() {
        // given
        String token = FabricForTests.incorrectNotExpiredAccessJwt();

        // when
        boolean isValid = jwtService.isTokenValid(token, testUser);

        // then
        assertThat(isValid).isFalse();
    }

    @Test
    void generateJWTAccessCorrectTest() {
        // when
        String accessToken = jwtService.generateJWTAccess(testUser);

        // then
        assertThat(accessToken).isNotNull();
        assertThat(jwtService.extractUserName(accessToken)).isEqualTo(FabricForTests.USERNAME);
        assertThat(jwtService.extractId(accessToken)).isEqualTo(FabricForTests.USER_ID);
        assertThat(jwtService.extractTokenType(accessToken)).isEqualTo("access");
        assertThat(jwtService.isTokenValid(accessToken, testUser)).isTrue();
    }

    @Test
    void generateJWTRefreshCorrectTest() {
        // when
        String refreshToken = jwtService.generateJWTRefresh(testUser);

        // then
        assertThat(refreshToken).isNotNull();
        assertThat(jwtService.extractUserName(refreshToken)).isEqualTo(FabricForTests.USERNAME);
        assertThat(jwtService.extractId(refreshToken)).isEqualTo(FabricForTests.USER_ID);
        assertThat(jwtService.extractTokenType(refreshToken)).isEqualTo("refresh");
        assertThat(jwtService.isTokenValid(refreshToken, testUser)).isTrue();
    }

    @Test
    void isTypeOfAccessCorrectTest() {
        // given
        String token = FabricForTests.correctNoExpiredAccessJwt();

        // when
        boolean isAccess = jwtService.isTypeOf(token, "access");

        // then
        assertThat(isAccess).isTrue();
    }

    @Test
    void isTypeOfRefreshCorrectTest() {
        // given
        String token = FabricForTests.correctNoExpiredRefreshJwt();

        // when
        boolean isRefresh = jwtService.isTypeOf(token, "refresh");

        // then
        assertThat(isRefresh).isTrue();
    }

    @Test
    void isTypeOfWithDifferentCaseTest() {
        // given
        String token = FabricForTests.correctNoExpiredAccessJwt();

        // when
        boolean isAccess = jwtService.isTypeOf(token, "ACCESS");

        // then
        assertThat(isAccess).isTrue();
    }

    @Test
    void isTypeOfWithSpacesTest() {
        // given
        String token = FabricForTests.correctNoExpiredAccessJwt();

        // when
        boolean isAccess = jwtService.isTypeOf(token, " access ");

        // then
        assertThat(isAccess).isTrue();
    }

    @Test
    void isTypeOfShouldReturnFalseForWrongTypeTest() {
        // given
        String token = FabricForTests.correctNoExpiredAccessJwt();

        // when
        boolean isRefresh = jwtService.isTypeOf(token, "refresh");

        // then
        assertThat(isRefresh).isFalse();
    }

    @Test
    void accessTokenExpirationShorterThanRefreshTest() {
        // when
        String accessToken = jwtService.generateJWTAccess(testUser);
        String refreshToken = jwtService.generateJWTRefresh(testUser);

        // then
        assertThat(accessToken).isNotEqualTo(refreshToken);
    }

    @Test
    void tokenWithEmptyClaimsTest() {
        // given
        String token = jwtService.generateJWTAccess(testUser);

        // when & then
        assertThat(jwtService.extractTokenType(token)).isEqualTo("access");
    }
}
