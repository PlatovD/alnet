package io.github.platovd.alnet.authentication.context;

import io.github.platovd.alnet.authentication.contex.SecurityContextWrapper;
import io.github.platovd.alnet.authentication.token.JWTAuthToken;
import io.github.platovd.alnet.authentication.util.AuthUtil;
import io.github.platovd.alnet.entity.Role;
import io.github.platovd.alnet.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class SecurityContextWrapperTest {
    private final String JWT = "jwt";
    private final Long USER_ID = 1L;
    private final String USERNAME = "Test";
    private final String ROLE = "USER";
    private final String ANONYMOUS_KEY = "KEY";
    private Authentication authentication;
    private User testUser;
    private UserDetails userDetails;

    private SecurityContextWrapper securityContextWrapper = new SecurityContextWrapper();

    @BeforeEach
    public void setUp() {
        testUser = User.builder().id(USER_ID).email("test@gmail.com").username(USERNAME).password("qwerty").role(
                List.of(Role.builder().name(ROLE).build())).build();
        userDetails = AuthUtil.fromUserToUserDetails(testUser);
        authentication = new JWTAuthToken(JWT, userDetails, List.of(new SimpleGrantedAuthority(ROLE)), true);
    }

    @Test
    public void authenticatedTest() {
        authentication.setAuthenticated(true);
        SecurityContextHolder.getContext().setAuthentication(authentication);
        assertThat(securityContextWrapper.isAuthenticated()).isTrue();
    }

    @Test
    public void isNotAuthenticatedTest() {
        SecurityContextHolder.getContext().setAuthentication(null);
        assertThat(securityContextWrapper.isAuthenticated()).isFalse();

        SecurityContextHolder.getContext().setAuthentication(new AnonymousAuthenticationToken("Key", userDetails,
                List.of(new SimpleGrantedAuthority(ROLE))));
        assertThat(securityContextWrapper.isAuthenticated()).isFalse();

        SecurityContextHolder.getContext().setAuthentication(authentication);
        authentication.setAuthenticated(false);
        assertThat(securityContextWrapper.isAuthenticated()).isFalse();
    }

    @Test
    public void getAuthUserInfoTest() {
        SecurityContextHolder.getContext().setAuthentication(authentication);
        assertThat(securityContextWrapper.getAuthenticatedUserInfo(UserDetails::getUsername))
                .isEqualTo(testUser.getUsername());
        assertThat(securityContextWrapper.getAuthenticatedUserInfo(UserDetails::getAuthorities)).extracting(
                "authority").contains("ROLE_USER");
    }

    @Test
    public void unAuthenticateTest() {
        securityContextWrapper.setAnonymousAuthKey(ANONYMOUS_KEY);
        SecurityContextHolder.getContext().setAuthentication(authentication);
        securityContextWrapper.unAuthenticate();
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isInstanceOf(AnonymousAuthenticationToken.class);
    }

    @Test
    public void setAuthenticateTest() {
        securityContextWrapper.setAnonymousAuthKey(ANONYMOUS_KEY);
        securityContextWrapper.setAuthentication(authentication);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isEqualTo(authentication);
    }
}
