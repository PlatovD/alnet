package io.github.platovd.alnet.authentication.userdetail;

import io.github.platovd.alnet.entity.Role;
import io.github.platovd.alnet.entity.User;
import io.github.platovd.alnet.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CustomUserDetailServiceTest {
    private final Long USER_ID = 1L;
    private final String USERNAME = "Test";
    private final String PASSWORD = "qwerty";
    private final String ROLE = "USER";
    private User testUser;
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CustomUserDetailService customUserDetailService;

    @BeforeEach
    public void setUp() {
        testUser = new User(
                USER_ID, USERNAME, PASSWORD, "test@gmail.com", List.of(new Role(ROLE, List.of())),
                null);
    }

    @Test
    public void loadUserByUsernameSuccessTest() {
        when(userRepository.findUserByUsername(USERNAME)).thenReturn(Optional.of(testUser));
        UserDetails userDetails = customUserDetailService.loadUserByUsername(USERNAME);
        assertThat(userDetails.getUsername()).isEqualTo(testUser.getUsername());
        assertThat(userDetails.getPassword()).isEqualTo(PASSWORD);
        assertThat(userDetails.getAuthorities()).extracting("authority").contains("ROLE_" + ROLE);
    }

    @Test
    public void loadUserByUsernameUnsuccessTest() {
        when(userRepository.findUserByUsername(USERNAME)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> customUserDetailService.loadUserByUsername(USERNAME))
                .isInstanceOf(UsernameNotFoundException.class);
    }
}
