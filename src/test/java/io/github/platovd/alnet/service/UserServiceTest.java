package io.github.platovd.alnet.service;

import io.github.platovd.alnet.authentication.contex.SecurityContextWrapper;
import io.github.platovd.alnet.authentication.token.JWTAuthToken;
import io.github.platovd.alnet.entity.User;
import io.github.platovd.alnet.exception.user.UserServiceException;
import io.github.platovd.alnet.repository.UserRepository;
import io.github.platovd.alnet.testutil.FabricForTests;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {
    @Mock
    private JWTAuthToken jwtAuthToken;

    @Mock
    private UserRepository userRepository;

    @Mock
    private SecurityContextWrapper securityContextWrapper;

    @InjectMocks
    private UserService userService;

    private User testUser;

    @BeforeEach
    public void setUp() {
        testUser = FabricForTests.testUser();
    }

    @Test
    public void createUserCorrectTest() {
        when(userRepository.existsByUsername(testUser.getUsername())).thenReturn(false);
        when(userRepository.existsByEmail(testUser.getEmail())).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(null);

        userService.create(testUser);
        verify(userRepository, times(1)).save(testUser);
    }

    @Test
    public void createUserLoginExistsTest() {
        when(userRepository.existsByUsername(testUser.getUsername())).thenReturn(true);

        assertThatThrownBy(() -> userService.create(testUser)).isInstanceOf(UserServiceException.class);
        verify(userRepository, times(0)).save(testUser);
    }

    @Test
    public void createUserEmailExistsTest() {
        when(userRepository.existsByUsername(testUser.getUsername())).thenReturn(false);
        when(userRepository.existsByEmail(testUser.getEmail())).thenReturn(true);

        assertThatThrownBy(() -> userService.create(testUser)).isInstanceOf(UserServiceException.class);
        verify(userRepository, times(0)).save(testUser);
    }

    @Test
    public void getByUsernameExistsTest() {
        when(userRepository.findUserByUsername(testUser.getUsername())).thenReturn(Optional.of(testUser));
        assertThat(userService.getByUsername(testUser.getUsername())).isEqualTo(testUser);
    }

    @Test
    public void getByUsernameNotExistsTest() {
        when(userRepository.findUserByUsername(testUser.getUsername())).thenReturn(Optional.empty());
        assertThatThrownBy(() -> userService.getByUsername(testUser.getUsername())).isInstanceOf(UserServiceException.class);
    }

    @Test
    public void getByIdExistsTest() {
        when(userRepository.findById(testUser.getUserId())).thenReturn(Optional.of(testUser));
        assertThat(userService.getById(testUser.getUserId())).isEqualTo(testUser);
    }

    @Test
    public void getByIdNotExistsTest() {
        when(userRepository.findById(testUser.getUserId())).thenReturn(Optional.empty());
        assertThatThrownBy(() -> userService.getById(testUser.getUserId())).isInstanceOf(UserServiceException.class);
    }
}
