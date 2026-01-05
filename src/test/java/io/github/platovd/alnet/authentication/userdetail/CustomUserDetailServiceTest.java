package io.github.platovd.alnet.authentication.userdetail;

import io.github.platovd.alnet.entity.User;
import io.github.platovd.alnet.repository.UserRepository;
import io.github.platovd.alnet.testutil.FabricForTests;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CustomUserDetailServiceTest {
    private User testUser;
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CustomUserDetailService customUserDetailService;

    @BeforeEach
    public void setUp() {
        testUser = FabricForTests.testUser();
    }

    @Test
    public void loadUserByUsernameSuccessTest() {
        when(userRepository.findUserByUsername(FabricForTests.USERNAME)).thenReturn(Optional.of(testUser));
        UserDetails userDetails = customUserDetailService.loadUserByUsername(FabricForTests.USERNAME);
        assertThat(userDetails.getUsername()).isEqualTo(testUser.getUsername());
        assertThat(userDetails.getPassword()).isEqualTo(FabricForTests.PASSWORD);
        assertThat(userDetails.getAuthorities()).extracting("authority").contains("ROLE_" + FabricForTests.ROLE);
    }

    @Test
    public void loadUserByUsernameUnsuccessTest() {
        when(userRepository.findUserByUsername(FabricForTests.USERNAME)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> customUserDetailService.loadUserByUsername(FabricForTests.USERNAME))
                .isInstanceOf(UsernameNotFoundException.class);
    }
}
