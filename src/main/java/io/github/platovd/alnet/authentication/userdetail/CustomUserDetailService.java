package io.github.platovd.alnet.authentication.userdetail;


import io.github.platovd.alnet.authentication.util.AuthUtil;
import io.github.platovd.alnet.repository.UserRepository;
import io.github.platovd.alnet.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@RequiredArgsConstructor
@Service
public class CustomUserDetailService implements UserDetailsService {
    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<User> user = userRepository.findUserByUsername(username);
        if (user.isEmpty())
            throw new UsernameNotFoundException("User " + username + " not found in database");
        return AuthUtil.fromUserToUserDetails(user.get());
    }
}
