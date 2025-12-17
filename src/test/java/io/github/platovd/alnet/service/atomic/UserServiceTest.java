package io.github.platovd.alnet.service.atomic;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.platovd.alnet.authentication.contex.SecurityContextWrapper;
import io.github.platovd.alnet.authentication.token.JWTAuthToken;
import io.github.platovd.alnet.entity.User;
import io.github.platovd.alnet.exception.user.*;
import io.github.platovd.alnet.repository.UserRepository;
import io.github.platovd.alnet.testutil.FabricForTests;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository repository;


    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private SecurityContextWrapper securityContextWrapper;

    @InjectMocks
    private UserService userService;

    @Test
    void createUserCorrectTest() {
        // given
        String encodedPassword = "encodedPassword";
        User savedUser = FabricForTests.testUser();

        when(repository.existsByUsername(FabricForTests.USERNAME)).thenReturn(false);
        when(repository.existsByEmail(FabricForTests.EMAIL)).thenReturn(false);
        when(passwordEncoder.encode(FabricForTests.PASSWORD)).thenReturn(encodedPassword);
        when(repository.save(any(User.class))).thenReturn(savedUser);

        // when
        User result = userService.create(FabricForTests.USERNAME, FabricForTests.EMAIL, FabricForTests.PASSWORD);

        // then
        assertThat(result).isEqualTo(savedUser);
        verify(repository).existsByUsername(FabricForTests.USERNAME);
        verify(repository).existsByEmail(FabricForTests.EMAIL);
        verify(passwordEncoder).encode(FabricForTests.PASSWORD);
        verify(repository).save(argThat(user ->
                user.getUsername().equals(FabricForTests.USERNAME) &&
                        user.getEmail().equals(FabricForTests.EMAIL) &&
                        user.getPassword().equals(encodedPassword)
        ));
    }

    @Test
    void createUserShouldThrowUsernameUsedExceptionTest() {
        // given
        when(repository.existsByUsername(FabricForTests.USERNAME)).thenReturn(true);

        // when & then
        assertThatThrownBy(() -> userService.create(FabricForTests.USERNAME, FabricForTests.EMAIL, FabricForTests.PASSWORD))
                .isInstanceOf(UsernameUsedException.class)
                .hasMessage("Username is already in use");

        verify(repository, never()).existsByEmail(anyString());
        verify(repository, never()).save(any(User.class));
    }

    @Test
    void createUserShouldThrowEmailUsedExceptionTest() {
        // given
        when(repository.existsByUsername(FabricForTests.USERNAME)).thenReturn(false);
        when(repository.existsByEmail(FabricForTests.EMAIL)).thenReturn(true);

        // when & then
        assertThatThrownBy(() -> userService.create(FabricForTests.USERNAME, FabricForTests.EMAIL, FabricForTests.PASSWORD))
                .isInstanceOf(EmailUsedException.class)
                .hasMessage("Email is already in use");

        verify(repository, never()).save(any(User.class));
    }

    @Test
    void getAllUsersByUsernameCorrectTest() {
        // given
        List<String> usernames = List.of(FabricForTests.USERNAME, "AnotherUser");
        List<User> expectedUsers = List.of(FabricForTests.testUser());
        when(repository.findAllByUsernames(anySet())).thenReturn(expectedUsers);

        // when
        List<User> result = userService.getAllUsersByUsername(usernames);

        // then
        assertThat(result).isEqualTo(expectedUsers);
        verify(repository).findAllByUsernames(Set.of(FabricForTests.USERNAME.strip(), "AnotherUser".strip()));
    }

    @Test
    void getAllUsersByUsernameWithEmptyListTest() {
        // given
        List<String> emptyUsernames = List.of();

        // when
        List<User> result = userService.getAllUsersByUsername(emptyUsernames);

        // then
        assertThat(result).isEmpty();
        verify(repository, never()).findAllByUsernames(anySet());
    }

    @Test
    void getByUsernameCorrectTest() {
        // given
        User expectedUser = FabricForTests.testUser();
        when(repository.findUserByUsername(FabricForTests.USERNAME))
                .thenReturn(Optional.of(expectedUser));

        // when
        User result = userService.getByUsername(FabricForTests.USERNAME);

        // then
        assertThat(result).isEqualTo(expectedUser);
        verify(repository).findUserByUsername(FabricForTests.USERNAME);
    }

    @Test
    void getByUsernameShouldThrowUsernameNotFoundExceptionTest() {
        // given
        when(repository.findUserByUsername(anyString())).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> userService.getByUsername(FabricForTests.USERNAME))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessage("Username wasn't found");

        verify(repository).findUserByUsername(FabricForTests.USERNAME);
    }

    @Test
    void getByIdCorrectTest() {
        // given
        User expectedUser = FabricForTests.testUser();
        when(repository.findById(FabricForTests.USER_ID))
                .thenReturn(Optional.of(expectedUser));

        // when
        User result = userService.getById(FabricForTests.USER_ID);

        // then
        assertThat(result).isEqualTo(expectedUser);
        verify(repository).findById(FabricForTests.USER_ID);
    }

    @Test
    void getByIdShouldThrowIdNotFoundExceptionTest() {
        // given
        when(repository.findById(anyLong())).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> userService.getById(FabricForTests.USER_ID))
                .isInstanceOf(IdNotFoundException.class)
                .hasMessage("Id wasn't found");

        verify(repository).findById(FabricForTests.USER_ID);
    }

    @Test
    void updateFullUserCorrectTest() {
        // given
        User existingUser = FabricForTests.testUser();
        User updatedUser = User.builder()
                .userId(FabricForTests.USER_ID)
                .username(FabricForTests.NEW_USERNAME)
                .email(FabricForTests.NEW_EMAIL)
                .password(FabricForTests.PASSWORD)
                .build();

        when(repository.findById(FabricForTests.USER_ID)).thenReturn(Optional.of(existingUser));
        when(repository.save(any(User.class))).thenReturn(updatedUser);

        // when
        User result = userService.updateFullUser(FabricForTests.USER_ID,
                FabricForTests.NEW_USERNAME, FabricForTests.NEW_EMAIL);

        // then
        assertThat(result.getUsername()).isEqualTo(FabricForTests.NEW_USERNAME);
        assertThat(result.getEmail()).isEqualTo(FabricForTests.NEW_EMAIL);
        verify(repository).findById(FabricForTests.USER_ID);
        verify(repository).save(argThat(user ->
                user.getUserId().equals(FabricForTests.USER_ID) &&
                        user.getUsername().equals(FabricForTests.NEW_USERNAME) &&
                        user.getEmail().equals(FabricForTests.NEW_EMAIL)
        ));
    }

    @Test
    void deleteUserByIdCorrectTest() {
        // when
        userService.deleteUserById(FabricForTests.USER_ID);

        // then
        verify(repository).removeUserByUserId(FabricForTests.USER_ID);
    }

    @Test
    void getCurrentUserWithJWTAuthTokenTest() {
        // given
        JWTAuthToken jwtToken = mock(JWTAuthToken.class);
        User expectedUser = FabricForTests.testUser();

        when(securityContextWrapper.isAuthenticated()).thenReturn(true);
        when(securityContextWrapper.getAuthentication()).thenReturn(jwtToken);
        when(jwtToken.getId()).thenReturn(FabricForTests.USER_ID); // Важно!
        when(repository.findById(FabricForTests.USER_ID)).thenReturn(Optional.of(expectedUser));

        // when
        User result = userService.getCurrentUser();

        // then
        assertThat(result).isEqualTo(expectedUser);
        verify(securityContextWrapper).isAuthenticated();
        verify(securityContextWrapper).getAuthentication();
        verify(jwtToken).getId();
        verify(repository).findById(FabricForTests.USER_ID);
    }

    @Test
    void getCurrentUserWithRegularAuthenticationTest() {
        // given
        Authentication auth = mock(Authentication.class);
        User expectedUser = FabricForTests.testUser();

        when(securityContextWrapper.isAuthenticated()).thenReturn(true);
        when(securityContextWrapper.getAuthentication()).thenReturn(auth);
        when(auth.getName()).thenReturn(FabricForTests.USERNAME);
        when(repository.findUserByUsername(FabricForTests.USERNAME)).thenReturn(Optional.of(expectedUser));

        // when
        User result = userService.getCurrentUser();

        // then
        assertThat(result).isEqualTo(expectedUser);
        verify(auth).getName();
        verify(repository).findUserByUsername(FabricForTests.USERNAME);
    }

    @Test
    void getCurrentUserShouldThrowUserServiceExceptionWhenNotAuthenticatedTest() {
        // given
        when(securityContextWrapper.isAuthenticated()).thenReturn(false);

        // when & then
        assertThatThrownBy(() -> userService.getCurrentUser())
                .isInstanceOf(UserServiceException.class)
                .hasMessageContaining("No authentication found");
    }

    @Test
    void isCurrentUserByUserCorrectTest() {
        // given
        User testUser = FabricForTests.testUser();
        Authentication auth = mock(Authentication.class);

        when(securityContextWrapper.isAuthenticated()).thenReturn(true);
        when(securityContextWrapper.getAuthentication()).thenReturn(auth);
        when(auth.getName()).thenReturn(testUser.getUsername()); // Пустая строка, а не null
        when(repository.findUserByUsername(testUser.getUsername())).thenReturn(Optional.of(testUser));

        // when
        boolean result = userService.isCurrentUserByUser(testUser);

        // then
        assertThat(result).isTrue();
        verify(auth).getName();
        verify(repository).findUserByUsername(testUser.getUsername());
    }

    @Test
    void isCurrentUserByIdCorrectTest() {
        // given
        User testUser = FabricForTests.testUser();
        Authentication auth = mock(Authentication.class);
        when(securityContextWrapper.isAuthenticated()).thenReturn(true);
        when(securityContextWrapper.getAuthentication()).thenReturn(auth);
        when(auth.getName()).thenReturn(FabricForTests.USERNAME);
        when(repository.findUserByUsername(anyString())).thenReturn(Optional.of(testUser));

        // when
        boolean result = userService.isCurrentUserById(FabricForTests.USER_ID);

        // then
        assertThat(result).isTrue();
    }

    @Test
    void getCurrentUserNameCorrectTest() {
        // given
        when(securityContextWrapper.isAuthenticated()).thenReturn(true);
        when(securityContextWrapper.getAuthenticatedUserInfo(any()))
                .thenReturn(FabricForTests.USERNAME);

        // when
        String username = userService.getCurrentUserName();

        // then
        assertThat(username).isEqualTo(FabricForTests.USERNAME);
        verify(securityContextWrapper).getAuthenticatedUserInfo(any());
    }

    @Test
    void getCurrentUserNameShouldThrowUserServiceExceptionTest() {
        // given
        when(securityContextWrapper.isAuthenticated()).thenReturn(false);

        // when & then
        assertThatThrownBy(() -> userService.getCurrentUserName())
                .isInstanceOf(UserServiceException.class)
                .hasMessageContaining("No authentication found");
    }

    @Test
    void unAuthenticateCorrectTest() {
        // when
        userService.unAuthenticate();

        // then
        verify(securityContextWrapper).unAuthenticate();
    }

    @Test
    void isCurrentUserWithUsernameFunctionTest() {
        // given
        Authentication auth = mock(Authentication.class);
        User testUser = FabricForTests.testUser();
        when(securityContextWrapper.isAuthenticated()).thenReturn(true);
        when(securityContextWrapper.getAuthentication()).thenReturn(auth);
        when(auth.getName()).thenReturn(testUser.getUsername());
        when(repository.findUserByUsername(anyString())).thenReturn(Optional.of(testUser));

        // when
        boolean result = userService.isCurrentUser(FabricForTests.USERNAME, User::getUsername);

        // then
        assertThat(result).isTrue();
    }

    @Test
    void isCurrentUserWithEmailFunctionTest() {
        // given
        Authentication auth = mock(Authentication.class);
        User testUser = FabricForTests.testUser();
        when(securityContextWrapper.isAuthenticated()).thenReturn(true);
        when(securityContextWrapper.getAuthentication()).thenReturn(auth);
        when(auth.getName()).thenReturn(testUser.getUsername());
        when(repository.findUserByUsername(anyString())).thenReturn(Optional.of(testUser));

        // when
        boolean result = userService.isCurrentUser(FabricForTests.EMAIL, User::getEmail);

        // then
        assertThat(result).isTrue();
    }
}